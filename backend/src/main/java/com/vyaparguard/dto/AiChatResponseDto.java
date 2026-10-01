package com.vyaparguard.dto;

import java.util.List;

public class AiChatResponseDto {
    private String answer;
    private List<String> keyInsights;
    private List<String> suggestedFollowUps;
    private String riskLevel;

    public AiChatResponseDto() {
    }

    public AiChatResponseDto(String answer, List<String> keyInsights, List<String> suggestedFollowUps, String riskLevel) {
        this.answer = answer;
        this.keyInsights = keyInsights;
        this.suggestedFollowUps = suggestedFollowUps;
        this.riskLevel = riskLevel;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getKeyInsights() {
        return keyInsights;
    }

    public void setKeyInsights(List<String> keyInsights) {
        this.keyInsights = keyInsights;
    }

    public List<String> getSuggestedFollowUps() {
        return suggestedFollowUps;
    }

    public void setSuggestedFollowUps(List<String> suggestedFollowUps) {
        this.suggestedFollowUps = suggestedFollowUps;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}
