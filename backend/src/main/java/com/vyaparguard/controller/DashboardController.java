package com.vyaparguard.controller;

import com.vyaparguard.dto.CashFlowResponseDto;
import com.vyaparguard.dto.DashboardSummaryDto;
import com.vyaparguard.service.FinancialEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final FinancialEngineService financialEngineService;

    public DashboardController(FinancialEngineService financialEngineService) {
        this.financialEngineService = financialEngineService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDto> getDashboard() {
        return ResponseEntity.ok(financialEngineService.getDashboardSummary());
    }

    @GetMapping("/cashflow")
    public ResponseEntity<CashFlowResponseDto> getCashFlow() {
        return ResponseEntity.ok(financialEngineService.getCashFlowTimeline());
    }
}
