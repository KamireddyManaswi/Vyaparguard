package com.vyaparguard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashFlowPointDto {
    private LocalDate date;
    private String label;
    private BigDecimal inflow;
    private BigDecimal outflow;
    private BigDecimal netChange;
    private BigDecimal balance;
    private BigDecimal threshold;
    private boolean isProjected;
    private boolean isShortage;

    public CashFlowPointDto() {
    }

    public CashFlowPointDto(LocalDate date, String label, BigDecimal inflow, BigDecimal outflow, BigDecimal netChange, BigDecimal balance, BigDecimal threshold, boolean isProjected, boolean isShortage) {
        this.date = date;
        this.label = label;
        this.inflow = inflow;
        this.outflow = outflow;
        this.netChange = netChange;
        this.balance = balance;
        this.threshold = threshold;
        this.isProjected = isProjected;
        this.isShortage = isShortage;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getInflow() {
        return inflow;
    }

    public void setInflow(BigDecimal inflow) {
        this.inflow = inflow;
    }

    public BigDecimal getOutflow() {
        return outflow;
    }

    public void setOutflow(BigDecimal outflow) {
        this.outflow = outflow;
    }

    public BigDecimal getNetChange() {
        return netChange;
    }

    public void setNetChange(BigDecimal netChange) {
        this.netChange = netChange;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public boolean isProjected() {
        return isProjected;
    }

    public void setProjected(boolean projected) {
        isProjected = projected;
    }

    public boolean isShortage() {
        return isShortage;
    }

    public void setShortage(boolean shortage) {
        isShortage = shortage;
    }
}
