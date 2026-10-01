package com.vyaparguard.dto;

import java.math.BigDecimal;

public class RecommendedActionDto {
    private String id;
    private String title;
    private String description;
    private String priority; // URGENT, HIGH, MEDIUM, LOW
    private String actionType; // FOLLOW_UP, DELAY_PAYMENT, REDUCE_EXPENSE, INCENTIVE
    private BigDecimal impactAmount;
    private String targetEntity;

    public RecommendedActionDto() {
    }

    public RecommendedActionDto(String id, String title, String description, String priority, String actionType, BigDecimal impactAmount, String targetEntity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.actionType = actionType;
        this.impactAmount = impactAmount;
        this.targetEntity = targetEntity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public BigDecimal getImpactAmount() {
        return impactAmount;
    }

    public void setImpactAmount(BigDecimal impactAmount) {
        this.impactAmount = impactAmount;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }
}
