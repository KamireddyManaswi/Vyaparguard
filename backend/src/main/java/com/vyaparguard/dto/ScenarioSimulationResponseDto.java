package com.vyaparguard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ScenarioSimulationResponseDto {
    private BigDecimal originalMinCash;
    private BigDecimal simulatedMinCash;
    private BigDecimal cashDifference;
    private boolean originalRisk;
    private boolean simulatedRisk;
    private String outcomeSummary;
    private LocalDate simulatedShortageDate;
    private List<CashFlowPointDto> originalForecast;
    private List<CashFlowPointDto> simulatedForecast;

    public ScenarioSimulationResponseDto() {
    }

    public BigDecimal getOriginalMinCash() {
        return originalMinCash;
    }

    public void setOriginalMinCash(BigDecimal originalMinCash) {
        this.originalMinCash = originalMinCash;
    }

    public BigDecimal getSimulatedMinCash() {
        return simulatedMinCash;
    }

    public void setSimulatedMinCash(BigDecimal simulatedMinCash) {
        this.simulatedMinCash = simulatedMinCash;
    }

    public BigDecimal getCashDifference() {
        return cashDifference;
    }

    public void setCashDifference(BigDecimal cashDifference) {
        this.cashDifference = cashDifference;
    }

    public boolean isOriginalRisk() {
        return originalRisk;
    }

    public void setOriginalRisk(boolean originalRisk) {
        this.originalRisk = originalRisk;
    }

    public boolean isSimulatedRisk() {
        return simulatedRisk;
    }

    public void setSimulatedRisk(boolean simulatedRisk) {
        this.simulatedRisk = simulatedRisk;
    }

    public String getOutcomeSummary() {
        return outcomeSummary;
    }

    public void setOutcomeSummary(String outcomeSummary) {
        this.outcomeSummary = outcomeSummary;
    }

    public LocalDate getSimulatedShortageDate() {
        return simulatedShortageDate;
    }

    public void setSimulatedShortageDate(LocalDate simulatedShortageDate) {
        this.simulatedShortageDate = simulatedShortageDate;
    }

    public List<CashFlowPointDto> getOriginalForecast() {
        return originalForecast;
    }

    public void setOriginalForecast(List<CashFlowPointDto> originalForecast) {
        this.originalForecast = originalForecast;
    }

    public List<CashFlowPointDto> getSimulatedForecast() {
        return simulatedForecast;
    }

    public void setSimulatedForecast(List<CashFlowPointDto> simulatedForecast) {
        this.simulatedForecast = simulatedForecast;
    }
}
