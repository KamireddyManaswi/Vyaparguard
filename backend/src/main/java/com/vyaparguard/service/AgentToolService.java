package com.vyaparguard.service;

import com.vyaparguard.dto.CashFlowPointDto;
import com.vyaparguard.dto.DashboardSummaryDto;
import com.vyaparguard.model.*;
import com.vyaparguard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AgentToolService {

    private final FinancialEngineService financialEngineService;
    private final InvoiceRepository invoiceRepository;
    private final PaymentObligationRepository paymentObligationRepository;
    private final ProposedActionRepository proposedActionRepository;

    public AgentToolService(FinancialEngineService financialEngineService,
                            InvoiceRepository invoiceRepository,
                            PaymentObligationRepository paymentObligationRepository,
                            ProposedActionRepository proposedActionRepository) {
        this.financialEngineService = financialEngineService;
        this.invoiceRepository = invoiceRepository;
        this.paymentObligationRepository = paymentObligationRepository;
        this.proposedActionRepository = proposedActionRepository;
    }

    /**
     * Tool 1: get_cash_position()
     */
    public Map<String, Object> getCashPosition() {
        DashboardSummaryDto summary = financialEngineService.getDashboardSummary();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("businessName", summary.getBusinessName());
        result.put("currentCash", summary.getCurrentCash());
        result.put("projected7DayCash", summary.getSevenDayProjectedCash());
        result.put("minimumThreshold", summary.getMinimumCashThreshold());
        result.put("minimumProjectedCash", summary.getMinimumProjectedCash());
        result.put("shortageAmount", summary.getShortageAmount());
        result.put("shortageDate", summary.getShortageDate() != null ? summary.getShortageDate().toString() : "None");
        result.put("hasRisk", summary.isHasRisk());
        result.put("riskMessage", summary.getRiskMessage());
        return result;
    }

    /**
     * Tool 2: get_overdue_invoices()
     */
    public List<Map<String, Object>> getOverdueInvoices() {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        LocalDate today = LocalDate.now();
        List<Invoice> overdueList = invoiceRepository.findOverdueInvoices(business.getId(), today);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Invoice inv : overdueList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", inv.getId());
            item.put("invoice", inv.getInvoiceNumber());
            item.put("customer", inv.getCustomerName());
            item.put("amount", inv.getAmount());
            item.put("dueDate", inv.getDueDate().toString());
            item.put("daysOverdue", ChronoUnit.DAYS.between(inv.getDueDate(), today));
            result.add(item);
        }
        return result;
    }

    /**
     * Tool 3: get_upcoming_payments()
     */
    public List<Map<String, Object>> getUpcomingPayments() {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        LocalDate today = LocalDate.now();
        List<PaymentObligation> upcomingList = paymentObligationRepository.findUpcomingPayments(business.getId(), today);

        List<Map<String, Object>> result = new ArrayList<>();
        for (PaymentObligation p : upcomingList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", p.getId());
            item.put("supplier", p.getSupplierName());
            item.put("amount", p.getAmount());
            item.put("dueDate", p.getDueDate().toString());
            item.put("category", p.getCategory());
            item.put("daysUntilDue", ChronoUnit.DAYS.between(today, p.getDueDate()));
            result.add(item);
        }
        return result;
    }

    /**
     * Tool 4: run_cashflow_scenario()
     */
    public Map<String, Object> runCashflowScenario(Integer collectionDelayDays,
                                                   Double expenseIncreasePercent,
                                                   String targetCustomer) {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        Long businessId = business.getId();
        LocalDate today = LocalDate.now();
        BigDecimal threshold = business.getMinimumCashThreshold();
        BigDecimal currentCash = financialEngineService.calculateCurrentCash(businessId, business.getOpeningCash());

        List<CashFlowPointDto> baselineForecast = financialEngineService.calculate7DayForecast(businessId, currentCash, threshold, today);

        BigDecimal baselineMin = baselineForecast.stream()
                .map(CashFlowPointDto::getBalance)
                .min(BigDecimal::compareTo)
                .orElse(currentCash);

        BigDecimal baselineEnding = baselineForecast.isEmpty() ? currentCash : baselineForecast.get(baselineForecast.size() - 1).getBalance();

        int delay = (collectionDelayDays != null && collectionDelayDays > 0) ? collectionDelayDays : 5;
        double expenseMultiplier = 1.0 + (expenseIncreasePercent != null ? expenseIncreasePercent / 100.0 : 0.0);

        // Find matching customer invoice if specified
        Invoice matchingInvoice = null;
        if (targetCustomer != null && !targetCustomer.trim().isEmpty()) {
            List<Invoice> allInvoices = invoiceRepository.findByBusinessIdOrderByDueDateAsc(businessId);
            String search = targetCustomer.trim().toLowerCase();
            for (Invoice inv : allInvoices) {
                if (inv.getCustomerName().toLowerCase().contains(search) || search.contains(inv.getCustomerName().toLowerCase())) {
                    matchingInvoice = inv;
                    break;
                }
            }
        }

        // Simulate 7-day progression with scenario adjustments
        BigDecimal runningBalance = currentCash;
        BigDecimal scenarioMin = currentCash;
        LocalDate shortageDate = null;

        for (int i = 1; i <= 7; i++) {
            LocalDate date = today.plusDays(i);

            // Calculate day inflows
            List<Invoice> dayInvoices = invoiceRepository.findPendingDueBetween(businessId, date, date);
            BigDecimal dayInflow = BigDecimal.ZERO;

            for (Invoice inv : dayInvoices) {
                if (matchingInvoice != null && inv.getId().equals(matchingInvoice.getId())) {
                    // This payment is delayed by 'delay' days, so it does not arrive today
                    continue;
                }
                dayInflow = dayInflow.add(inv.getAmount());
            }

            // If target invoice had arrived earlier or today but was delayed into the future, inflow is removed
            // If target invoice delay pushes it into this day from an earlier day:
            if (matchingInvoice != null && matchingInvoice.getDueDate().plusDays(delay).equals(date)) {
                dayInflow = dayInflow.add(matchingInvoice.getAmount());
            }

            // Calculate day outflows
            List<PaymentObligation> dayPayments = paymentObligationRepository.findPendingDueBetween(businessId, date, date);
            BigDecimal dayOutflow = BigDecimal.ZERO;
            for (PaymentObligation p : dayPayments) {
                BigDecimal pAmt = p.getAmount();
                if (expenseMultiplier > 1.0) {
                    pAmt = pAmt.multiply(BigDecimal.valueOf(expenseMultiplier));
                }
                dayOutflow = dayOutflow.add(pAmt);
            }

            runningBalance = runningBalance.add(dayInflow).subtract(dayOutflow);
            if (runningBalance.compareTo(scenarioMin) < 0) {
                scenarioMin = runningBalance;
            }
            if (runningBalance.compareTo(threshold) < 0 && shortageDate == null) {
                shortageDate = date;
            }
        }

        BigDecimal scenarioEnding = runningBalance;
        BigDecimal shortageAmount = scenarioMin.compareTo(threshold) < 0
                ? threshold.subtract(scenarioMin)
                : BigDecimal.ZERO;

        String riskLevel = "LOW";
        if (shortageAmount.compareTo(new BigDecimal("30000")) > 0) {
            riskLevel = "CRITICAL";
        } else if (shortageAmount.compareTo(BigDecimal.ZERO) > 0) {
            riskLevel = "HIGH";
        } else if (scenarioMin.compareTo(threshold.multiply(new BigDecimal("1.2"))) < 0) {
            riskLevel = "MEDIUM";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("targetCustomer", matchingInvoice != null ? matchingInvoice.getCustomerName() : (targetCustomer != null ? targetCustomer : "General Invoices"));
        result.put("delayedInvoiceAmount", matchingInvoice != null ? matchingInvoice.getAmount() : BigDecimal.ZERO);
        result.put("collectionDelayDays", delay);
        result.put("baselineMinimumCash", baselineMin);
        result.put("scenarioMinimumCash", scenarioMin);
        result.put("baselineEndingCash", baselineEnding);
        result.put("scenarioEndingCash", scenarioEnding);
        result.put("shortageDate", shortageDate != null ? shortageDate.toString() : "None");
        result.put("shortageAmount", shortageAmount);
        result.put("riskLevel", riskLevel);
        return result;
    }

    /**
     * Tool 5: create_action()
     */
    @Transactional
    public ProposedAction createAction(String title, String description, String priority, String category) {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        String prio = (priority != null && !priority.trim().isEmpty()) ? priority.trim().toUpperCase() : "MEDIUM";
        String cat = (category != null && !category.trim().isEmpty()) ? category.trim().toUpperCase() : "GENERAL";
        ProposedAction action = new ProposedAction(
                business.getId(),
                title,
                description,
                prio,
                cat,
                "PROPOSED"
        );
        return proposedActionRepository.save(action);
    }

    public List<ProposedAction> getProposedActions() {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        return proposedActionRepository.findByBusinessIdOrderByCreatedAtDesc(business.getId());
    }

    @Transactional
    public ProposedAction approveAction(Long id) {
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Action not found with id: " + id));
        action.setStatus("APPROVED");
        return proposedActionRepository.save(action);
    }

    @Transactional
    public ProposedAction rejectAction(Long id) {
        ProposedAction action = proposedActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Action not found with id: " + id));
        action.setStatus("REJECTED");
        return proposedActionRepository.save(action);
    }
}
