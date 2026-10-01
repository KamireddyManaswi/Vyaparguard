package com.vyaparguard.dto;

import com.vyaparguard.model.ProposedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgentQueryResponse {
    private boolean success;
    private boolean apiKeyMissing;
    private String message;
    private String summary;
    private List<String> findings = new ArrayList<>();
    private List<String> evidence = new ArrayList<>();
    private String risk; // LOW, MEDIUM, HIGH, CRITICAL
    private List<String> recommendedActions = new ArrayList<>();
    private List<String> assumptions = new ArrayList<>();
    private List<String> toolCallsExecuted = new ArrayList<>();
    private Map<String, Object> scenarioComparison;
    private List<ProposedAction> createdActions = new ArrayList<>();

    public AgentQueryResponse() {
    }

    public static AgentQueryResponse missingKey(String customMsg) {
        AgentQueryResponse resp = new AgentQueryResponse();
        resp.setSuccess(false);
        resp.setApiKeyMissing(true);
        resp.setMessage(customMsg != null ? customMsg : "AI assistant requires an LLM API key. Financial dashboard remains available.");
        return resp;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isApiKeyMissing() {
        return apiKeyMissing;
    }

    public void setApiKeyMissing(boolean apiKeyMissing) {
        this.apiKeyMissing = apiKeyMissing;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getFindings() {
        return findings;
    }

    public void setFindings(List<String> findings) {
        this.findings = findings;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }

    public String getRisk() {
        return risk;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public List<String> getAssumptions() {
        return assumptions;
    }

    public void setAssumptions(List<String> assumptions) {
        this.assumptions = assumptions;
    }

    public List<String> getToolCallsExecuted() {
        return toolCallsExecuted;
    }

    public void setToolCallsExecuted(List<String> toolCallsExecuted) {
        this.toolCallsExecuted = toolCallsExecuted;
    }

    public Map<String, Object> getScenarioComparison() {
        return scenarioComparison;
    }

    public void setScenarioComparison(Map<String, Object> scenarioComparison) {
        this.scenarioComparison = scenarioComparison;
    }

    public List<ProposedAction> getCreatedActions() {
        return createdActions;
    }

    public void setCreatedActions(List<ProposedAction> createdActions) {
        this.createdActions = createdActions;
    }
}
