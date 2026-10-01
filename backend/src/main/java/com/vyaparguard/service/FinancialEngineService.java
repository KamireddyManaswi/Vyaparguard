package com.vyaparguard.service;

import com.vyaparguard.dto.*;
import com.vyaparguard.model.*;
import com.vyaparguard.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinancialEngineService {

    private final BusinessRepository businessRepository;
    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentObligationRepository paymentObligationRepository;

    public FinancialEngineService(BusinessRepository businessRepository,
                                  TransactionRepository transactionRepository,
                                  InvoiceRepository invoiceRepository,
                                  PaymentObligationRepository paymentObligationRepository) {
        this.businessRepository = businessRepository;
        this.transactionRepository = transactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentObligationRepository = paymentObligationRepository;
    }

    public Business getOrCreateDefaultBusiness() {
        return businessRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            Business business = new Business("Sri Lakshmi General Stores",
                    new BigDecimal("120000.00"),
                    new BigDecimal("50000.00"));
            return businessRepository.save(business);
        });
    }

    public BigDecimal calculateCurrentCash(Long businessId, BigDecimal openingCash) {
        BigDecimal actualInflows = transactionRepository.sumAmountByBusinessIdAndType(businessId, TransactionType.CASH_IN);
        BigDecimal actualOutflows = transactionRepository.sumAmountByBusinessIdAndType(businessId, TransactionType.CASH_OUT);
        return openingCash.add(actualInflows).subtract(actualOutflows);
    }

    public DashboardSummaryDto getDashboardSummary() {
        Business business = getOrCreateDefaultBusiness();
        Long businessId = business.getId();
        LocalDate today = LocalDate.now();

        BigDecimal currentCash = calculateCurrentCash(businessId, business.getOpeningCash());
        BigDecimal totalReceivables = invoiceRepository.sumTotalReceivables(businessId);
        BigDecimal overdueReceivables = invoiceRepository.sumOverdueReceivables(businessId, today);
        BigDecimal upcomingPayments = paymentObligationRepository.sumUpcomingPayments(businessId, today);

        List<CashFlowPointDto> forecast7Days = calculate7DayForecast(businessId, currentCash, business.getMinimumCashThreshold(), today);

        BigDecimal minProjectedCash = currentCash;
        LocalDate shortageDate = null;
        boolean hasRisk = false;

        for (CashFlowPointDto point : forecast7Days) {
            if (point.getBalance().compareTo(minProjectedCash) < 0) {
                minProjectedCash = point.getBalance();
            }
            if (point.getBalance().compareTo(business.getMinimumCashThreshold()) < 0 && shortageDate == null) {
                shortageDate = point.getDate();
                hasRisk = true;
            }
        }

        BigDecimal shortageAmount = BigDecimal.ZERO;
        if (minProjectedCash.compareTo(business.getMinimumCashThreshold()) < 0) {
            shortageAmount = business.getMinimumCashThreshold().subtract(minProjectedCash);
            hasRisk = true;
        }

        String riskMessage = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        if (hasRisk && shortageDate != null) {
            riskMessage = String.format("Projected balance falls to \u20B9%,d, below your \u20B9%,d safety threshold on %s.",
                    minProjectedCash.longValue(),
                    business.getMinimumCashThreshold().longValue(),
                    shortageDate.format(formatter));
        }

        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(businessId, today);
        List<PaymentObligation> upcomingPaymentList = paymentObligationRepository.findUpcomingPayments(businessId, today);

        List<RecommendedActionDto> recommendedActions = generateRecommendations(business, currentCash, minProjectedCash, shortageAmount, shortageDate, overdueInvoices, upcomingPaymentList);

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setBusinessId(businessId);
        dto.setBusinessName(business.getName());
        dto.setOpeningCash(business.getOpeningCash());
        dto.setMinimumCashThreshold(business.getMinimumCashThreshold());
        dto.setCurrentCash(currentCash);
        dto.setTotalReceivables(totalReceivables);
        dto.setOverdueReceivables(overdueReceivables);
        dto.setUpcomingPayments(upcomingPayments);
        dto.setSevenDayProjectedCash(forecast7Days.isEmpty() ? currentCash : forecast7Days.get(forecast7Days.size() - 1).getBalance());
        dto.setMinimumProjectedCash(minProjectedCash);
        dto.setShortageAmount(shortageAmount);
        dto.setShortageDate(shortageDate);
        dto.setHasRisk(hasRisk);
        dto.setRiskMessage(riskMessage);
        dto.setForecast7Days(forecast7Days);
        dto.setOverdueInvoices(overdueInvoices);
        dto.setUpcomingPaymentList(upcomingPaymentList);
        dto.setRecommendedActions(recommendedActions);

        return dto;
    }

    public List<CashFlowPointDto> calculate7DayForecast(Long businessId, BigDecimal startingCash, BigDecimal threshold, LocalDate startDate) {
        List<CashFlowPointDto> forecast = new ArrayList<>();
        BigDecimal runningBalance = startingCash;

        // Day 0 (Today)
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM d");
        forecast.add(new CashFlowPointDto(
                startDate,
                startDate.format(labelFormatter) + " (Today)",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                runningBalance,
                threshold,
                false,
                runningBalance.compareTo(threshold) < 0
        ));

        // Days 1 through 7
        for (int i = 1; i <= 7; i++) {
            LocalDate date = startDate.plusDays(i);

            // Invoices due on this date
            List<Invoice> dayInvoices = invoiceRepository.findPendingDueBetween(businessId, date, date);
            BigDecimal dayInflow = dayInvoices.stream()
                    .map(Invoice::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Payment obligations due on this date
            List<PaymentObligation> dayPayments = paymentObligationRepository.findPendingDueBetween(businessId, date, date);
            BigDecimal dayOutflow = dayPayments.stream()
                    .map(PaymentObligation::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal netChange = dayInflow.subtract(dayOutflow);
            runningBalance = runningBalance.add(netChange);

            forecast.add(new CashFlowPointDto(
                    date,
                    date.format(labelFormatter),
                    dayInflow,
                    dayOutflow,
                    netChange,
                    runningBalance,
                    threshold,
                    true,
                    runningBalance.compareTo(threshold) < 0
            ));
        }

        return forecast;
    }

    public CashFlowResponseDto getCashFlowTimeline() {
        Business business = getOrCreateDefaultBusiness();
        Long businessId = business.getId();
        LocalDate today = LocalDate.now();
        BigDecimal threshold = business.getMinimumCashThreshold();
        BigDecimal currentCash = calculateCurrentCash(businessId, business.getOpeningCash());

        // Historical 7 days
        LocalDate histStart = today.minusDays(7);
        List<Transaction> histTxns = transactionRepository.findByBusinessIdAndDateBetweenOrderByDateAsc(businessId, histStart, today.minusDays(1));

        Map<LocalDate, BigDecimal> inflowsByDate = new HashMap<>();
        Map<LocalDate, BigDecimal> outflowsByDate = new HashMap<>();

        for (Transaction txn : histTxns) {
            if (txn.getType() == TransactionType.CASH_IN) {
                inflowsByDate.merge(txn.getDate(), txn.getAmount(), BigDecimal::add);
            } else {
                outflowsByDate.merge(txn.getDate(), txn.getAmount(), BigDecimal::add);
            }
        }

        // To calculate historical balance accurately, we can backtrack from currentCash or compute forward
        List<CashFlowPointDto> historical = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM d");

        // Backtrack starting point for histStart
        BigDecimal histRunningBalance = currentCash;
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.minusDays(i);
            BigDecimal in = inflowsByDate.getOrDefault(d, BigDecimal.ZERO);
            BigDecimal out = outflowsByDate.getOrDefault(d, BigDecimal.ZERO);
            histRunningBalance = histRunningBalance.subtract(in).add(out);
        }

        for (int i = 7; i >= 1; i--) {
            LocalDate d = today.minusDays(i);
            BigDecimal in = inflowsByDate.getOrDefault(d, BigDecimal.ZERO);
            BigDecimal out = outflowsByDate.getOrDefault(d, BigDecimal.ZERO);
            BigDecimal net = in.subtract(out);
            histRunningBalance = histRunningBalance.add(net);

            historical.add(new CashFlowPointDto(
                    d,
                    d.format(labelFormatter),
                    in,
                    out,
                    net,
                    histRunningBalance,
                    threshold,
                    false,
                    histRunningBalance.compareTo(threshold) < 0
            ));
        }

        // 7-day forecast
        List<CashFlowPointDto> forecast = calculate7DayForecast(businessId, currentCash, threshold, today);

        // Combined timeline: historical + forecast (forecast includes Day 0 to Day 7)
        List<CashFlowPointDto> combined = new ArrayList<>(historical);
        combined.addAll(forecast);

        return new CashFlowResponseDto(currentCash, threshold, historical, forecast, combined);
    }

    private List<RecommendedActionDto> generateRecommendations(Business business,
                                                              BigDecimal currentCash,
                                                              BigDecimal minCash,
                                                              BigDecimal shortageAmount,
                                                              LocalDate shortageDate,
                                                              List<Invoice> overdueInvoices,
                                                              List<PaymentObligation> upcomingPayments) {
        List<RecommendedActionDto> list = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM d");

        // 1. Overdue collection action
        if (!overdueInvoices.isEmpty()) {
            Invoice topOverdue = overdueInvoices.get(0);
            list.add(new RecommendedActionDto(
                    "act-1",
                    "Follow up with " + topOverdue.getCustomerName(),
                    String.format("Overdue invoice %s of \u20B9%,d is pending since %s. Collecting this immediately restores your cash buffer.",
                            topOverdue.getInvoiceNumber(), topOverdue.getAmount().longValue(), topOverdue.getDueDate().format(dtf)),
                    "URGENT",
                    "FOLLOW_UP",
                    topOverdue.getAmount(),
                    topOverdue.getCustomerName()
            ));
        }

        // 2. Supplier obligation action
        if (!upcomingPayments.isEmpty()) {
            PaymentObligation topPayment = upcomingPayments.get(0);
            list.add(new RecommendedActionDto(
                    "act-2",
                    "Review upcoming payment to " + topPayment.getSupplierName(),
                    String.format("Payment of \u20B9%,d is due on %s. Request a 7-day credit extension or negotiate 50%% partial payment to preserve liquidity.",
                            topPayment.getAmount().longValue(), topPayment.getDueDate().format(dtf)),
                    minCash.compareTo(business.getMinimumCashThreshold()) < 0 ? "HIGH" : "MEDIUM",
                    "DELAY_PAYMENT",
                    topPayment.getAmount(),
                    topPayment.getSupplierName()
            ));
        }

        // 3. Discretionary spending or early payment discount
        if (shortageAmount.compareTo(BigDecimal.ZERO) > 0) {
            list.add(new RecommendedActionDto(
                    "act-3",
                    "Hold discretionary non-inventory disbursements",
                    String.format("Projected balance dips \u20B9%,d below your safety reserve. Pausing non-essential expenses for the next 7 days maintains compliance.",
                            shortageAmount.longValue()),
                    "HIGH",
                    "REDUCE_EXPENSE",
                    shortageAmount,
                    "Discretionary Spend"
            ));
        } else {
            list.add(new RecommendedActionDto(
                    "act-3",
                    "Maintain healthy supplier discounts",
                    "Cash position is safe above \u20B950,000 threshold. Avail 1-2% early payment discounts from primary wholesalers.",
                    "LOW",
                    "INCENTIVE",
                    new BigDecimal("1500"),
                    "Suppliers"
            ));
        }

        return list;
    }

    public ScenarioSimulationResponseDto simulateScenario(ScenarioSimulationRequestDto request) {
        Business business = getOrCreateDefaultBusiness();
        Long businessId = business.getId();
        LocalDate today = LocalDate.now();
        BigDecimal threshold = business.getMinimumCashThreshold();
        BigDecimal currentCash = calculateCurrentCash(businessId, business.getOpeningCash());

        List<CashFlowPointDto> originalForecast = calculate7DayForecast(businessId, currentCash, threshold, today);

        BigDecimal originalMin = originalForecast.stream()
                .map(CashFlowPointDto::getBalance)
                .min(BigDecimal::compareTo)
                .orElse(currentCash);

        boolean originalRisk = originalMin.compareTo(threshold) < 0;

        // Apply simulation modifications
        List<CashFlowPointDto> simulatedForecast = new ArrayList<>();
        BigDecimal runningBalance = currentCash;

        Set<Long> collectedInvoices = request.getCollectedInvoiceIds() != null
                ? new HashSet<>(request.getCollectedInvoiceIds())
                : Collections.emptySet();

        Set<Long> delayedPayments = request.getDelayedPaymentIds() != null
                ? new HashSet<>(request.getDelayedPaymentIds())
                : Collections.emptySet();

        int delayDays = request.getDelayDays() > 0 ? request.getDelayDays() : 7;
        BigDecimal additionalInflow = request.getAdditionalInflow() != null ? request.getAdditionalInflow() : BigDecimal.ZERO;
        BigDecimal expenseCutRate = request.getExpenseReductionPercent() != null
                ? request.getExpenseReductionPercent().divide(new BigDecimal("100"))
                : BigDecimal.ZERO;

        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MMM d");

        // Day 0
        simulatedForecast.add(new CashFlowPointDto(
                today,
                today.format(labelFormatter) + " (Today)",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                runningBalance,
                threshold,
                false,
                runningBalance.compareTo(threshold) < 0
        ));

        // If collected invoices are simulated as collected on Day 1 or Day 2
        BigDecimal collectedAmount = BigDecimal.ZERO;
        if (!collectedInvoices.isEmpty()) {
            List<Invoice> invoices = invoiceRepository.findAllById(collectedInvoices);
            for (Invoice inv : invoices) {
                collectedAmount = collectedAmount.add(inv.getAmount());
            }
        }

        for (int i = 1; i <= 7; i++) {
            LocalDate date = today.plusDays(i);

            List<Invoice> dayInvoices = invoiceRepository.findPendingDueBetween(businessId, date, date);
            BigDecimal dayInflow = dayInvoices.stream()
                    .map(Invoice::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Add collected overdue invoice injection on day 1 or 2
            if (i == 1 && collectedAmount.compareTo(BigDecimal.ZERO) > 0) {
                dayInflow = dayInflow.add(collectedAmount);
            }
            if (i == 1 && additionalInflow.compareTo(BigDecimal.ZERO) > 0) {
                dayInflow = dayInflow.add(additionalInflow);
            }

            List<PaymentObligation> dayPayments = paymentObligationRepository.findPendingDueBetween(businessId, date, date);
            BigDecimal dayOutflow = BigDecimal.ZERO;

            for (PaymentObligation p : dayPayments) {
                if (delayedPayments.contains(p.getId())) {
                    // Payment is pushed beyond current 7-day window or delayed
                    continue;
                }
                BigDecimal pAmount = p.getAmount();
                if (expenseCutRate.compareTo(BigDecimal.ZERO) > 0 && "Discretionary".equalsIgnoreCase(p.getCategory())) {
                    pAmount = pAmount.multiply(BigDecimal.ONE.subtract(expenseCutRate));
                }
                dayOutflow = dayOutflow.add(pAmount);
            }

            BigDecimal netChange = dayInflow.subtract(dayOutflow);
            runningBalance = runningBalance.add(netChange);

            simulatedForecast.add(new CashFlowPointDto(
                    date,
                    date.format(labelFormatter),
                    dayInflow,
                    dayOutflow,
                    netChange,
                    runningBalance,
                    threshold,
                    true,
                    runningBalance.compareTo(threshold) < 0
            ));
        }

        BigDecimal simulatedMin = simulatedForecast.stream()
                .map(CashFlowPointDto::getBalance)
                .min(BigDecimal::compareTo)
                .orElse(currentCash);

        boolean simulatedRisk = simulatedMin.compareTo(threshold) < 0;
        BigDecimal diff = simulatedMin.subtract(originalMin);

        LocalDate simShortageDate = null;
        for (CashFlowPointDto p : simulatedForecast) {
            if (p.getBalance().compareTo(threshold) < 0) {
                simShortageDate = p.getDate();
                break;
            }
        }

        String summary;
        if (originalRisk && !simulatedRisk) {
            summary = String.format("Success! Risk eliminated. Minimum cash improved by \u20B9%,d, keeping balance safely above the \u20B9%,d threshold throughout the next 7 days.",
                    diff.abs().longValue(), threshold.longValue());
        } else if (simulatedRisk) {
            summary = String.format("Notice: Cash balance still dips below threshold on %s to \u20B9%,d, though improved by \u20B9%,d. Consider combining invoice collection with supplier payment deferral.",
                    simShortageDate != null ? simShortageDate.format(DateTimeFormatter.ofPattern("MMM d")) : "the horizon",
                    simulatedMin.longValue(), diff.longValue());
        } else {
            summary = String.format("Simulation complete. Minimum projected cash remains strong at \u20B9%,d (+\u20B9%,d change).",
                    simulatedMin.longValue(), diff.longValue());
        }

        ScenarioSimulationResponseDto resp = new ScenarioSimulationResponseDto();
        resp.setOriginalMinCash(originalMin);
        resp.setSimulatedMinCash(simulatedMin);
        resp.setCashDifference(diff);
        resp.setOriginalRisk(originalRisk);
        resp.setSimulatedRisk(simulatedRisk);
        resp.setOutcomeSummary(summary);
        resp.setSimulatedShortageDate(simShortageDate);
        resp.setOriginalForecast(originalForecast);
        resp.setSimulatedForecast(simulatedForecast);

        return resp;
    }
}
