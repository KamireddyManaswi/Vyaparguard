package com.vyaparguard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vyaparguard.dto.AgentQueryRequest;
import com.vyaparguard.dto.AgentQueryResponse;
import com.vyaparguard.model.ProposedAction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AgentExecutionService {

    private final AgentToolService agentToolService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${vyaparguard.ai.api-key:}")
    private String configuredApiKey;

    private String runtimeApiKey = null;

    public AgentExecutionService(AgentToolService agentToolService, ObjectMapper objectMapper) {
        this.agentToolService = agentToolService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public void setRuntimeApiKey(String key) {
        this.runtimeApiKey = key;
    }

    public String resolveApiKey(String requestKey) {
        if (requestKey != null && !requestKey.trim().isEmpty()) {
            return requestKey.trim();
        }
        if (runtimeApiKey != null && !runtimeApiKey.trim().isEmpty()) {
            return runtimeApiKey.trim();
        }
        if (configuredApiKey != null && !configuredApiKey.trim().isEmpty()) {
            return configuredApiKey.trim();
        }
        String envLlm = System.getenv("LLM_API_KEY");
        if (envLlm != null && !envLlm.trim().isEmpty()) {
            return envLlm.trim();
        }
        String envGemini = System.getenv("GEMINI_API_KEY");
        if (envGemini != null && !envGemini.trim().isEmpty()) {
            return envGemini.trim();
        }
        String envOpenAi = System.getenv("OPENAI_API_KEY");
        if (envOpenAi != null && !envOpenAi.trim().isEmpty()) {
            return envOpenAi.trim();
        }
        return null;
    }

    public boolean hasApiKey() {
        return resolveApiKey(null) != null;
    }

    public AgentQueryResponse executeAgent(AgentQueryRequest request) {
        String apiKey = resolveApiKey(request.getApiKey());

        // Requirement: If LLM API key is missing: Show clear message: "AI assistant requires an LLM API key. Financial dashboard remains available." Do NOT fake AI responses.
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return AgentQueryResponse.missingKey("AI assistant requires an LLM API key. Financial dashboard remains available.");
        }

        try {
            if (apiKey.startsWith("AIza")) {
                return executeWithGemini(request.getQuery(), apiKey);
            } else {
                return executeWithOpenAI(request.getQuery(), apiKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
            AgentQueryResponse errorResp = new AgentQueryResponse();
            errorResp.setSuccess(false);
            errorResp.setMessage("Error communicating with AI model: " + e.getMessage());
            return errorResp;
        }
    }

    /**
     * Executes Agent using Google Gemini with function calling
     */
    private AgentQueryResponse executeWithGemini(String userQuery, String apiKey) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        List<String> toolCallsExecuted = new ArrayList<>();
        List<ProposedAction> createdActions = new ArrayList<>();
        Map<String, Object> scenarioComparison = null;

        // Build Gemini request with tools and system instruction
        ObjectNode root = objectMapper.createObjectNode();

        // System Instruction
        ObjectNode sysInst = root.putObject("systemInstruction");
        ObjectNode sysParts = sysInst.putArray("parts").addObject();
        sysParts.put("text", "You are the VyaparGuard AI Cash Advisor for an Indian small business. " +
                "You must NEVER calculate financial numbers yourself. ALWAYS use tools to retrieve deterministic data. " +
                "Available tools: get_cash_position, get_overdue_invoices, get_upcoming_payments, run_cashflow_scenario, create_action. " +
                "When asked 'What should I do today?', use tools to inspect data, then call create_action 2-3 times to save proposed actions in DB. " +
                "When asked about late payments or 'What happens if [Customer] pays X days late?', call run_cashflow_scenario. " +
                "After tool calls complete, respond in valid JSON with keys: " +
                "summary (string), findings (list of strings), evidence (list of strings with exact numbers), " +
                "risk (LOW/MEDIUM/HIGH/CRITICAL), recommendedActions (list of strings), assumptions (list of strings).");

        // Tools declaration
        ArrayNode toolsArray = root.putArray("tools");
        ObjectNode functionDeclarations = toolsArray.addObject();
        ArrayNode funcs = functionDeclarations.putArray("functionDeclarations");

        // 1. get_cash_position
        ObjectNode f1 = funcs.addObject();
        f1.put("name", "get_cash_position");
        f1.put("description", "Returns verified current cash, 7-day projected cash, minimum threshold, shortage amount, and shortage date.");

        // 2. get_overdue_invoices
        ObjectNode f2 = funcs.addObject();
        f2.put("name", "get_overdue_invoices");
        f2.put("description", "Returns all overdue customer invoices with customer names, invoice numbers, amounts, and days overdue.");

        // 3. get_upcoming_payments
        ObjectNode f3 = funcs.addObject();
        f3.put("name", "get_upcoming_payments");
        f3.put("description", "Returns upcoming trade supplier payments with amounts and due dates.");

        // 4. run_cashflow_scenario
        ObjectNode f4 = funcs.addObject();
        f4.put("name", "run_cashflow_scenario");
        f4.put("description", "Runs scenario simulation comparing baseline cash curve vs delayed customer payment or increased expenses.");
        ObjectNode f4Params = f4.putObject("parameters");
        f4Params.put("type", "OBJECT");
        ObjectNode f4Props = f4Params.putObject("properties");
        f4Props.putObject("collectionDelayDays").put("type", "INTEGER").put("description", "Number of days customer payment is delayed (e.g. 5)");
        f4Props.putObject("expenseIncreasePercent").put("type", "NUMBER").put("description", "Percentage increase in expenses (optional)");
        f4Props.putObject("targetCustomer").put("type", "STRING").put("description", "Name of customer whose payment is delayed (e.g. ABC Traders)");

        // 5. create_action
        ObjectNode f5 = funcs.addObject();
        f5.put("name", "create_action");
        f5.put("description", "Saves a proposed action to the database for user approval.");
        ObjectNode f5Params = f5.putObject("parameters");
        f5Params.put("type", "OBJECT");
        ObjectNode f5Props = f5Params.putObject("properties");
        f5Props.putObject("title").put("type", "STRING").put("description", "Brief title of the action");
        f5Props.putObject("description").put("type", "STRING").put("description", "Detailed instructions and amount");
        f5Props.putObject("priority").put("type", "STRING").put("description", "HIGH, MEDIUM, or LOW");
        f5Props.putObject("category").put("type", "STRING").put("description", "RECEIVABLES, PAYABLES, or EXPENSES");
        ArrayNode f5Req = f5Params.putArray("required");
        f5Req.add("title");
        f5Req.add("description");
        f5Req.add("priority");

        // Contents (conversation history)
        ArrayNode contents = root.putArray("contents");
        ObjectNode userTurn = contents.addObject();
        userTurn.put("role", "user");
        userTurn.putArray("parts").addObject().put("text", userQuery);

        // First call to Gemini
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        JsonNode resJson = objectMapper.readTree(response.getBody());
        JsonNode candidate = resJson.path("candidates").get(0).path("content");
        JsonNode parts = candidate.path("parts");

        // Tool execution loop (up to 3 turns)
        int maxTurns = 3;
        while (parts != null && maxTurns-- > 0) {
            boolean hasToolCalls = false;
            ObjectNode modelTurn = contents.addObject();
            modelTurn.put("role", "model");
            ArrayNode modelParts = modelTurn.putArray("parts");

            ObjectNode toolTurn = contents.addObject();
            toolTurn.put("role", "function");
            ArrayNode functionResponses = toolTurn.putArray("parts");

            for (JsonNode part : parts) {
                if (part.has("functionCall")) {
                    hasToolCalls = true;
                    modelParts.add(part);

                    String funcName = part.path("functionCall").path("name").asText();
                    JsonNode args = part.path("functionCall").path("args");

                    JsonNode toolResultNode = null;

                    if ("get_cash_position".equals(funcName)) {
                        toolCallsExecuted.add("Checking cash position");
                        Map<String, Object> data = agentToolService.getCashPosition();
                        toolResultNode = objectMapper.valueToTree(data);
                    } else if ("get_overdue_invoices".equals(funcName)) {
                        toolCallsExecuted.add("Checking overdue invoices");
                        List<Map<String, Object>> data = agentToolService.getOverdueInvoices();
                        toolResultNode = objectMapper.valueToTree(data);
                    } else if ("get_upcoming_payments".equals(funcName)) {
                        toolCallsExecuted.add("Checking upcoming payments");
                        List<Map<String, Object>> data = agentToolService.getUpcomingPayments();
                        toolResultNode = objectMapper.valueToTree(data);
                    } else if ("run_cashflow_scenario".equals(funcName)) {
                        toolCallsExecuted.add("Running forecast scenario");
                        int delay = args.path("collectionDelayDays").asInt(5);
                        double exp = args.path("expenseIncreasePercent").asDouble(0.0);
                        String cust = args.path("targetCustomer").asText("ABC Traders");
                        Map<String, Object> data = agentToolService.runCashflowScenario(delay, exp, cust);
                        scenarioComparison = data;
                        toolResultNode = objectMapper.valueToTree(data);
                    } else if ("create_action".equals(funcName)) {
                        String title = args.path("title").asText("Follow up on dues");
                        String desc = args.path("description").asText();
                        String prio = args.path("priority").asText("MEDIUM");
                        String cat = args.path("category").asText("RECEIVABLES");
                        ProposedAction action = agentToolService.createAction(title, desc, prio, cat);
                        createdActions.add(action);
                        toolCallsExecuted.add("Created proposed action: " + title);
                        toolResultNode = objectMapper.valueToTree(action);
                    }

                    ObjectNode fnRespPart = functionResponses.addObject();
                    ObjectNode fnResponseObj = fnRespPart.putObject("functionResponse");
                    fnResponseObj.put("name", funcName);
                    
                    if (toolResultNode != null && toolResultNode.isArray()) {
                        ObjectNode wrapped = objectMapper.createObjectNode();
                        wrapped.set("items", toolResultNode);
                        fnResponseObj.set("response", wrapped);
                    } else if (toolResultNode != null && toolResultNode.isObject()) {
                        fnResponseObj.set("response", toolResultNode);
                    } else {
                        ObjectNode wrapped = objectMapper.createObjectNode();
                        wrapped.put("status", "completed");
                        fnResponseObj.set("response", wrapped);
                    }
                }
            }

            if (!hasToolCalls) {
                // Final answer reached
                break;
            }

            // Post back function responses to Gemini
            HttpEntity<String> nextEntity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);
            ResponseEntity<String> nextRes = restTemplate.postForEntity(url, nextEntity, String.class);
            resJson = objectMapper.readTree(nextRes.getBody());
            candidate = resJson.path("candidates").get(0).path("content");
            parts = candidate.path("parts");
        }

        // Extract text response
        StringBuilder fullText = new StringBuilder();
        if (parts != null) {
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    fullText.append(part.path("text").asText());
                }
            }
        }

        return parseStructuredAgentResponse(fullText.toString(), toolCallsExecuted, createdActions, scenarioComparison);
    }

    /**
     * Executes Agent using OpenAI API
     */
    private AgentQueryResponse executeWithOpenAI(String userQuery, String apiKey) throws Exception {
        String url = "https://api.openai.com/v1/chat/completions";

        List<String> toolCallsExecuted = new ArrayList<>();
        List<ProposedAction> createdActions = new ArrayList<>();
        Map<String, Object> scenarioComparison = null;

        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", "gpt-4o-mini");

        ArrayNode messages = root.putArray("messages");
        ObjectNode sysMsg = messages.addObject();
        sysMsg.put("role", "system");
        sysMsg.put("content", "You are the VyaparGuard AI Cash Advisor for Indian small businesses. " +
                "You must NEVER calculate financial numbers yourself. ALWAYS use tools to retrieve deterministic data. " +
                "When asked 'What should I do today?', call create_action 2-3 times to save proposed actions in DB. " +
                "When asked about customer delays, call run_cashflow_scenario. " +
                "Respond in valid JSON with keys: summary, findings, evidence, risk (LOW/MEDIUM/HIGH/CRITICAL), recommendedActions, assumptions.");

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userQuery);

        // Define tools for OpenAI
        ArrayNode tools = root.putArray("tools");
        addOpenAiTool(tools, "get_cash_position", "Returns current cash, 7-day projected cash, minimum threshold, shortage amount, and shortage date.");
        addOpenAiTool(tools, "get_overdue_invoices", "Returns all overdue customer invoices with amounts and days overdue.");
        addOpenAiTool(tools, "get_upcoming_payments", "Returns upcoming trade supplier payments with amounts and due dates.");
        addOpenAiToolWithParams(tools, "run_cashflow_scenario", "Runs cashflow scenario comparing baseline vs delay",
                Arrays.asList("collectionDelayDays", "targetCustomer"));
        addOpenAiToolWithParams(tools, "create_action", "Creates proposed action in database",
                Arrays.asList("title", "description", "priority"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        JsonNode resJson = objectMapper.readTree(response.getBody());
        JsonNode choice = resJson.path("choices").get(0).path("message");

        if (choice.has("tool_calls")) {
            ArrayNode toolCalls = (ArrayNode) choice.path("tool_calls");
            messages.add(choice);

            for (JsonNode tc : toolCalls) {
                String funcName = tc.path("function").path("name").asText();
                String argsStr = tc.path("function").path("arguments").asText();
                JsonNode args = objectMapper.readTree(argsStr);

                String toolResultStr = "{}";
                if ("get_cash_position".equals(funcName)) {
                    toolCallsExecuted.add("Checking cash position");
                    toolResultStr = objectMapper.writeValueAsString(agentToolService.getCashPosition());
                } else if ("get_overdue_invoices".equals(funcName)) {
                    toolCallsExecuted.add("Checking overdue invoices");
                    toolResultStr = objectMapper.writeValueAsString(agentToolService.getOverdueInvoices());
                } else if ("get_upcoming_payments".equals(funcName)) {
                    toolCallsExecuted.add("Checking upcoming payments");
                    toolResultStr = objectMapper.writeValueAsString(agentToolService.getUpcomingPayments());
                } else if ("run_cashflow_scenario".equals(funcName)) {
                    toolCallsExecuted.add("Running forecast scenario");
                    int delay = args.path("collectionDelayDays").asInt(5);
                    double exp = args.path("expenseIncreasePercent").asDouble(0.0);
                    String cust = args.path("targetCustomer").asText("ABC Traders");
                    Map<String, Object> data = agentToolService.runCashflowScenario(delay, exp, cust);
                    scenarioComparison = data;
                    toolResultStr = objectMapper.writeValueAsString(data);
                } else if ("create_action".equals(funcName)) {
                    String title = args.path("title").asText("Action");
                    String desc = args.path("description").asText();
                    String prio = args.path("priority").asText("MEDIUM");
                    String cat = args.path("category").asText("RECEIVABLES");
                    ProposedAction act = agentToolService.createAction(title, desc, prio, cat);
                    createdActions.add(act);
                    toolCallsExecuted.add("Created proposed action: " + title);
                    toolResultStr = objectMapper.writeValueAsString(act);
                }

                ObjectNode toolRespMsg = messages.addObject();
                toolRespMsg.put("role", "tool");
                toolRespMsg.put("tool_call_id", tc.path("id").asText());
                toolRespMsg.put("content", toolResultStr);
            }

            HttpEntity<String> secondEntity = new HttpEntity<>(objectMapper.writeValueAsString(root), headers);
            ResponseEntity<String> secondRes = restTemplate.postForEntity(url, secondEntity, String.class);
            JsonNode secondJson = objectMapper.readTree(secondRes.getBody());
            String finalContent = secondJson.path("choices").get(0).path("message").path("content").asText();
            return parseStructuredAgentResponse(finalContent, toolCallsExecuted, createdActions, scenarioComparison);
        } else {
            String content = choice.path("content").asText();
            return parseStructuredAgentResponse(content, toolCallsExecuted, createdActions, scenarioComparison);
        }
    }

    private void addOpenAiTool(ArrayNode tools, String name, String desc) {
        ObjectNode t = tools.addObject();
        t.put("type", "function");
        ObjectNode f = t.putObject("function");
        f.put("name", name);
        f.put("description", desc);
        ObjectNode p = f.putObject("parameters");
        p.put("type", "object");
        p.putObject("properties");
    }

    private void addOpenAiToolWithParams(ArrayNode tools, String name, String desc, List<String> paramNames) {
        ObjectNode t = tools.addObject();
        t.put("type", "function");
        ObjectNode f = t.putObject("function");
        f.put("name", name);
        f.put("description", desc);
        ObjectNode p = f.putObject("parameters");
        p.put("type", "object");
        ObjectNode props = p.putObject("properties");
        for (String param : paramNames) {
            props.putObject(param).put("type", "string");
        }
    }

    private AgentQueryResponse parseStructuredAgentResponse(String rawText,
                                                            List<String> toolCalls,
                                                            List<ProposedAction> createdActions,
                                                            Map<String, Object> scenarioComparison) {
        AgentQueryResponse response = new AgentQueryResponse();
        response.setSuccess(true);
        response.setApiKeyMissing(false);
        response.setToolCallsExecuted(toolCalls);
        response.setCreatedActions(createdActions);
        response.setScenarioComparison(scenarioComparison);

        try {
            // Clean markdown code blocks if LLM wrapped JSON in ```json ... ```
            String cleanJson = rawText.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            JsonNode parsed = objectMapper.readTree(cleanJson);
            response.setSummary(parsed.path("summary").asText(rawText));

            if (parsed.has("findings") && parsed.get("findings").isArray()) {
                for (JsonNode f : parsed.get("findings")) {
                    response.getFindings().add(f.asText());
                }
            }

            if (parsed.has("evidence") && parsed.get("evidence").isArray()) {
                for (JsonNode e : parsed.get("evidence")) {
                    response.getEvidence().add(e.asText());
                }
            }

            response.setRisk(parsed.path("risk").asText("MEDIUM").toUpperCase());

            if (parsed.has("recommendedActions") && parsed.get("recommendedActions").isArray()) {
                for (JsonNode r : parsed.get("recommendedActions")) {
                    response.getRecommendedActions().add(r.asText());
                }
            }

            if (parsed.has("assumptions") && parsed.get("assumptions").isArray()) {
                for (JsonNode a : parsed.get("assumptions")) {
                    response.getAssumptions().add(a.asText());
                }
            }

            return response;
        } catch (Exception parseEx) {
            // If LLM returned structured markdown text instead of pure JSON, map it gracefully
            response.setSummary(rawText);
            response.setRisk(rawText.toUpperCase().contains("CRITICAL") ? "CRITICAL" :
                    rawText.toUpperCase().contains("HIGH") ? "HIGH" : "MEDIUM");
            response.getFindings().add("Generated based on deterministic backend tool execution");
            return response;
        }
    }
}
