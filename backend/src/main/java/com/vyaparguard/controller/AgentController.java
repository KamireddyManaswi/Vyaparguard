package com.vyaparguard.controller;

import com.vyaparguard.dto.AgentQueryRequest;
import com.vyaparguard.dto.AgentQueryResponse;
import com.vyaparguard.model.ProposedAction;
import com.vyaparguard.service.AgentExecutionService;
import com.vyaparguard.service.AgentToolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentExecutionService agentExecutionService;
    private final AgentToolService agentToolService;

    public AgentController(AgentExecutionService agentExecutionService, AgentToolService agentToolService) {
        this.agentExecutionService = agentExecutionService;
        this.agentToolService = agentToolService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentQueryResponse> chat(@RequestBody AgentQueryRequest request) {
        return ResponseEntity.ok(agentExecutionService.executeAgent(request));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Collections.singletonMap("apiKeyConfigured", agentExecutionService.hasApiKey()));
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> setConfig(@RequestBody Map<String, String> body) {
        String key = body.get("apiKey");
        agentExecutionService.setRuntimeApiKey(key);
        return ResponseEntity.ok(Collections.singletonMap("apiKeyConfigured", agentExecutionService.hasApiKey()));
    }

    @GetMapping("/actions")
    public ResponseEntity<List<ProposedAction>> getActions() {
        return ResponseEntity.ok(agentToolService.getProposedActions());
    }

    @PostMapping("/actions/{id}/approve")
    public ResponseEntity<ProposedAction> approveAction(@PathVariable Long id) {
        return ResponseEntity.ok(agentToolService.approveAction(id));
    }

    @PostMapping("/actions/{id}/reject")
    public ResponseEntity<ProposedAction> rejectAction(@PathVariable Long id) {
        return ResponseEntity.ok(agentToolService.rejectAction(id));
    }

    // Direct Tool Endpoints for verification & inspection
    @GetMapping("/tools/cash-position")
    public ResponseEntity<Map<String, Object>> toolCashPosition() {
        return ResponseEntity.ok(agentToolService.getCashPosition());
    }

    @GetMapping("/tools/overdue-invoices")
    public ResponseEntity<List<Map<String, Object>>> toolOverdueInvoices() {
        return ResponseEntity.ok(agentToolService.getOverdueInvoices());
    }

    @GetMapping("/tools/upcoming-payments")
    public ResponseEntity<List<Map<String, Object>>> toolUpcomingPayments() {
        return ResponseEntity.ok(agentToolService.getUpcomingPayments());
    }

    @PostMapping("/tools/run-scenario")
    public ResponseEntity<Map<String, Object>> toolRunScenario(@RequestBody Map<String, Object> params) {
        Integer delay = params.get("collectionDelayDays") != null ? ((Number) params.get("collectionDelayDays")).intValue() : 5;
        Double exp = params.get("expenseIncreasePercent") != null ? ((Number) params.get("expenseIncreasePercent")).doubleValue() : 0.0;
        String cust = params.get("targetCustomer") != null ? params.get("targetCustomer").toString() : "ABC Traders";
        return ResponseEntity.ok(agentToolService.runCashflowScenario(delay, exp, cust));
    }

    @PostMapping("/tools/create-action")
    public ResponseEntity<ProposedAction> toolCreateAction(@RequestBody Map<String, String> body) {
        String title = body.getOrDefault("title", "Follow up on customer dues");
        String desc = body.getOrDefault("description", "Action description");
        String prio = body.getOrDefault("priority", "HIGH");
        String cat = body.getOrDefault("category", "RECEIVABLES");
        return ResponseEntity.ok(agentToolService.createAction(title, desc, prio, cat));
    }
}
