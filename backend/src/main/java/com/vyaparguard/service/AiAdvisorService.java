package com.vyaparguard.service;

import com.vyaparguard.dto.*;
import com.vyaparguard.model.Invoice;
import com.vyaparguard.model.PaymentObligation;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AiAdvisorService {

    private final FinancialEngineService financialEngineService;

    public AiAdvisorService(FinancialEngineService financialEngineService) {
        this.financialEngineService = financialEngineService;
    }

    public AiChatResponseDto answerQuestion(AiChatRequestDto request) {
        DashboardSummaryDto summary = financialEngineService.getDashboardSummary();
        String q = request.getQuestion() != null ? request.getQuestion().trim().toLowerCase() : "";

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String shortageDateStr = summary.getShortageDate() != null ? summary.getShortageDate().format(dtf) : "None";

        StringBuilder answer = new StringBuilder();
        List<String> keyInsights = new ArrayList<>();
        List<String> suggestedFollowUps = new ArrayList<>();
        String riskLevel = summary.isHasRisk() ? "HIGH" : "NORMAL";

        if (q.contains("why") || q.contains("shortage") || q.contains("fall") || q.contains("drop") || q.contains("below")) {
            answer.append(String.format("**Cash Shortage Analysis for %s:**\n\n", summary.getBusinessName()));
            answer.append(String.format("Your current cash is reasonably healthy at **\u20B9%,d**, but our 7-day forecast projects your balance to drop to **\u20B9%,d** on **%s**, falling **\u20B9%,d** below your safety threshold of **\u20B9%,d**.\n\n",
                    summary.getCurrentCash().longValue(),
                    summary.getMinimumProjectedCash().longValue(),
                    shortageDateStr,
                    summary.getShortageAmount().longValue(),
                    summary.getMinimumCashThreshold().longValue()));

            answer.append("**Primary Root Causes:**\n");
            answer.append(String.format("1. **Heavy Supplier Obligations:** You have **\u20B9%,d** in supplier payments due over the coming week (including ITC Distribution Hub and Hindustan FMCG Wholesalers).\n",
                    summary.getUpcomingPayments().longValue()));
            answer.append(String.format("2. **Delayed Customer Inflows:** You have **\u20B9%,d** tied up in **overdue customer invoices** (notably Mehta Kirana Mart and Anand Sweets & Snacks) that have not arrived.\n",
                    summary.getOverdueReceivables().longValue()));
            answer.append("3. **Asymmetric Timing:** Outflows are scheduled earlier than collections, draining liquidity before replacements materialize.\n");

            keyInsights.add(String.format("Shortage of \u20B9%,d projected on %s", summary.getShortageAmount().longValue(), shortageDateStr));
            keyInsights.add(String.format("Overdue receivables (\u20B9%,d) exceed the shortage amount (\u20B9%,d)", summary.getOverdueReceivables().longValue(), summary.getShortageAmount().longValue()));
            keyInsights.add("Immediate invoice recovery eliminates 100% of the risk");

            suggestedFollowUps.add("How do I fix the projected cash deficit?");
            suggestedFollowUps.add("Which overdue invoices should I collect first?");
            suggestedFollowUps.add("Can I negotiate payment terms with ITC or Hindustan FMCG?");

        } else if (q.contains("anand") || q.contains("what if") || q.contains("collect") || q.contains("pay") || q.contains("simulate")) {
            answer.append("**Simulation Impact Assessment:**\n\n");
            answer.append("Collecting outstanding invoices early dramatically alters your liquidity curve:\n\n");
            answer.append(String.format("- **Total Overdue Receivables:** \u20B9%,d\n", summary.getOverdueReceivables().longValue()));
            answer.append(String.format("- **Projected Deficit:** \u20B9%,d\n\n", summary.getShortageAmount().longValue()));
            answer.append("If Anand Sweets & Snacks (\u20B918,000) or Mehta Kirana Mart (\u20B924,500) settles within 48 hours, your projected minimum balance rises back above the \u20B950,000 safety threshold, fully averting the cash crunch without taking any loans.");

            keyInsights.add("Simulating receipt of \u20B925,000 lifts balance above threshold");
            keyInsights.add("No external credit or loan required");

            suggestedFollowUps.add("Simulate this scenario in the Scenarios tab");
            suggestedFollowUps.add("Draft a payment reminder WhatsApp message for Anand Sweets");

        } else if (q.contains("supplier") || q.contains("prioritize") || q.contains("vendor") || q.contains("delay")) {
            answer.append("**Supplier Payment Prioritization Strategy:**\n\n");
            answer.append("To protect cash while preserving vital distributor relationships:\n\n");
            if (!summary.getUpcomingPaymentList().isEmpty()) {
                for (int i = 0; i < Math.min(3, summary.getUpcomingPaymentList().size()); i++) {
                    PaymentObligation p = summary.getUpcomingPaymentList().get(i);
                    answer.append(String.format("%d. **%s** — \u20B9%,d (Due %s, %s)\n",
                            i + 1, p.getSupplierName(), p.getAmount().longValue(),
                            p.getDueDate().format(DateTimeFormatter.ofPattern("MMM d")), p.getCategory()));
                }
            }
            answer.append("\n**Action Plan:** Request a 5 to 7 day credit extension on the largest inventory order or make a 50% split payment. This keeps \u20B920,000+ in your account during the critical shortage window.");

            keyInsights.add("Stagger supplier disbursements across 10 days rather than 3 days");
            keyInsights.add("Preserve emergency reserve above \u20B950,000");

            suggestedFollowUps.add("What is our current cash runway?");
            suggestedFollowUps.add("Show all overdue invoices");

        } else {
            // General business overview
            answer.append(String.format("**Financial Health Summary for %s:**\n\n", summary.getBusinessName()));
            answer.append(String.format("- **Current Bank & Cash Balance:** \u20B9%,d\n", summary.getCurrentCash().longValue()));
            answer.append(String.format("- **Customer Receivables:** \u20B9%,d (\u20B9%,d currently overdue)\n",
                    summary.getTotalReceivables().longValue(), summary.getOverdueReceivables().longValue()));
            answer.append(String.format("- **Upcoming Supplier Payables:** \u20B9%,d\n", summary.getUpcomingPayments().longValue()));
            answer.append(String.format("- **7-Day Minimum Cash:** \u20B9%,d (Safety Threshold: \u20B9%,d)\n\n",
                    summary.getMinimumProjectedCash().longValue(), summary.getMinimumCashThreshold().longValue()));

            if (summary.isHasRisk()) {
                answer.append(String.format("⚠️ **Risk Alert:** %s\n\nWe recommend initiating immediate follow-ups with overdue debtors and scheduling supplier payments in batches.", summary.getRiskMessage()));
            } else {
                answer.append("✅ **Status:** Your cash reserves are currently projected to stay above the minimum safety threshold.");
            }

            keyInsights.add(String.format("Cash: \u20B9%,d | Overdue: \u20B9%,d", summary.getCurrentCash().longValue(), summary.getOverdueReceivables().longValue()));
            keyInsights.add(summary.isHasRisk() ? "Action required to avert shortage" : "Healthy cash buffer");

            suggestedFollowUps.add("Why is cash falling below threshold?");
            suggestedFollowUps.add("Which suppliers should I prioritize?");
            suggestedFollowUps.add("How can I improve customer collection speed?");
        }

        return new AiChatResponseDto(answer.toString(), keyInsights, suggestedFollowUps, riskLevel);
    }
}
