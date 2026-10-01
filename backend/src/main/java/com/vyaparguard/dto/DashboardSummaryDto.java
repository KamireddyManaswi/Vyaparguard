package com.vyaparguard.dto;

import com.vyaparguard.model.Invoice;
import com.vyaparguard.model.PaymentObligation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardSummaryDto {
    private Long businessId;
    private String businessName;
    private BigDecimal openingCash;
    private BigDecimal minimumCashThreshold;
    private BigDecimal currentCash;
    private BigDecimal totalReceivables;
    private BigDecimal overdueReceivables;
    private BigDecimal upcomingPayments;
    private BigDecimal sevenDayProjectedCash;
    private BigDecimal minimumProjectedCash;
    private BigDecimal shortageAmount;
    private LocalDate shortageDate;
    private boolean hasRisk;
    private String riskMessage;
    private List<CashFlowPointDto> forecast7Days;
    private List<Invoice> overdueInvoices;
    private List<PaymentObligation> upcomingPaymentList;
    private List<RecommendedActionDto> recommendedActions;

    public DashboardSummaryDto() {
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public BigDecimal getOpeningCash() {
        return openingCash;
    }

    public void setOpeningCash(BigDecimal openingCash) {
        this.openingCash = openingCash;
    }

    public BigDecimal getMinimumCashThreshold() {
        return minimumCashThreshold;
    }

    public void setMinimumCashThreshold(BigDecimal minimumCashThreshold) {
        this.minimumCashThreshold = minimumCashThreshold;
    }

    public BigDecimal getCurrentCash() {
        return currentCash;
    }

    public void setCurrentCash(BigDecimal currentCash) {
        this.currentCash = currentCash;
    }

    public BigDecimal getTotalReceivables() {
        return totalReceivables;
    }

    public void setTotalReceivables(BigDecimal totalReceivables) {
        this.totalReceivables = totalReceivables;
    }

    public BigDecimal getOverdueReceivables() {
        return overdueReceivables;
    }

    public void setOverdueReceivables(BigDecimal overdueReceivables) {
        this.overdueReceivables = overdueReceivables;
    }

    public BigDecimal getUpcomingPayments() {
        return upcomingPayments;
    }

    public void setUpcomingPayments(BigDecimal upcomingPayments) {
        this.upcomingPayments = upcomingPayments;
    }

    public BigDecimal getSevenDayProjectedCash() {
        return sevenDayProjectedCash;
    }

    public void setSevenDayProjectedCash(BigDecimal sevenDayProjectedCash) {
        this.sevenDayProjectedCash = sevenDayProjectedCash;
    }

    public BigDecimal getMinimumProjectedCash() {
        return minimumProjectedCash;
    }

    public void setMinimumProjectedCash(BigDecimal minimumProjectedCash) {
        this.minimumProjectedCash = minimumProjectedCash;
    }

    public BigDecimal getShortageAmount() {
        return shortageAmount;
    }

    public void setShortageAmount(BigDecimal shortageAmount) {
        this.shortageAmount = shortageAmount;
    }

    public LocalDate getShortageDate() {
        return shortageDate;
    }

    public void setShortageDate(LocalDate shortageDate) {
        this.shortageDate = shortageDate;
    }

    public boolean isHasRisk() {
        return hasRisk;
    }

    public void setHasRisk(boolean hasRisk) {
        this.hasRisk = hasRisk;
    }

    public String getRiskMessage() {
        return riskMessage;
    }

    public void setRiskMessage(String riskMessage) {
        this.riskMessage = riskMessage;
    }

    public List<CashFlowPointDto> getForecast7Days() {
        return forecast7Days;
    }

    public void setForecast7Days(List<CashFlowPointDto> forecast7Days) {
        this.forecast7Days = forecast7Days;
    }

    public List<Invoice> getOverdueInvoices() {
        return overdueInvoices;
    }

    public void setOverdueInvoices(List<Invoice> overdueInvoices) {
        this.overdueInvoices = overdueInvoices;
    }

    public List<PaymentObligation> getUpcomingPaymentList() {
        return upcomingPaymentList;
    }

    public void setUpcomingPaymentList(List<PaymentObligation> upcomingPaymentList) {
        this.upcomingPaymentList = upcomingPaymentList;
    }

    public List<RecommendedActionDto> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<RecommendedActionDto> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }
}
