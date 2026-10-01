package com.vyaparguard.dto;

public class AgentQueryRequest {
    private String query;
    private String apiKey;

    public AgentQueryRequest() {
    }

    public AgentQueryRequest(String query, String apiKey) {
        this.query = query;
        this.apiKey = apiKey;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
