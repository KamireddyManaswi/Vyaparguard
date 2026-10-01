package com.vyaparguard.dto;

import java.math.BigDecimal;
import java.util.List;

public class CashFlowResponseDto {
    private BigDecimal currentCash;
    private BigDecimal minimumCashThreshold;
    private List<CashFlowPointDto> historical;
    private List<CashFlowPointDto> forecast;
    private List<CashFlowPointDto> combinedTimeline;

    public CashFlowResponseDto() {
    }

    public CashFlowResponseDto(BigDecimal currentCash, BigDecimal minimumCashThreshold, List<CashFlowPointDto> historical, List<CashFlowPointDto> forecast, List<CashFlowPointDto> combinedTimeline) {
        this.currentCash = currentCash;
        this.minimumCashThreshold = minimumCashThreshold;
        this.historical = historical;
        this.forecast = forecast;
        this.combinedTimeline = combinedTimeline;
    }

    public BigDecimal getCurrentCash() {
        return currentCash;
    }

    public void setCurrentCash(BigDecimal currentCash) {
        this.currentCash = currentCash;
    }

    public BigDecimal getMinimumCashThreshold() {
        return minimumCashThreshold;
    }

    public void setMinimumCashThreshold(BigDecimal minimumCashThreshold) {
        this.minimumCashThreshold = minimumCashThreshold;
    }

    public List<CashFlowPointDto> getHistorical() {
        return historical;
    }

    public void setHistorical(List<CashFlowPointDto> historical) {
        this.historical = historical;
    }

    public List<CashFlowPointDto> getForecast() {
        return forecast;
    }

    public void setForecast(List<CashFlowPointDto> forecast) {
        this.forecast = forecast;
    }

    public List<CashFlowPointDto> getCombinedTimeline() {
        return combinedTimeline;
    }

    public void setCombinedTimeline(List<CashFlowPointDto> combinedTimeline) {
        this.combinedTimeline = combinedTimeline;
    }
}
