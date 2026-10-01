package com.vyaparguard.dto;

import java.math.BigDecimal;
import java.util.List;

public class ScenarioSimulationRequestDto {
    private List<Long> collectedInvoiceIds;
    private List<Long> delayedPaymentIds;
    private int delayDays;
    private BigDecimal additionalInflow;
    private BigDecimal expenseReductionPercent;

    public ScenarioSimulationRequestDto() {
    }

    public List<Long> getCollectedInvoiceIds() {
        return collectedInvoiceIds;
    }

    public void setCollectedInvoiceIds(List<Long> collectedInvoiceIds) {
        this.collectedInvoiceIds = collectedInvoiceIds;
    }

    public List<Long> getDelayedPaymentIds() {
        return delayedPaymentIds;
    }

    public void setDelayedPaymentIds(List<Long> delayedPaymentIds) {
        this.delayedPaymentIds = delayedPaymentIds;
    }

    public int getDelayDays() {
        return delayDays;
    }

    public void setDelayDays(int delayDays) {
        this.delayDays = delayDays;
    }

    public BigDecimal getAdditionalInflow() {
        return additionalInflow;
    }

    public void setAdditionalInflow(BigDecimal additionalInflow) {
        this.additionalInflow = additionalInflow;
    }

    public BigDecimal getExpenseReductionPercent() {
        return expenseReductionPercent;
    }

    public void setExpenseReductionPercent(BigDecimal expenseReductionPercent) {
        this.expenseReductionPercent = expenseReductionPercent;
    }
}
