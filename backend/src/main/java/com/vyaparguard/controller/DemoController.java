package com.vyaparguard.controller;

import com.vyaparguard.dto.DashboardSummaryDto;
import com.vyaparguard.service.DemoDataSeederService;
import com.vyaparguard.service.FinancialEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoController {

    private final DemoDataSeederService demoDataSeederService;
    private final FinancialEngineService financialEngineService;

    public DemoController(DemoDataSeederService demoDataSeederService,
                          FinancialEngineService financialEngineService) {
        this.demoDataSeederService = demoDataSeederService;
        this.financialEngineService = financialEngineService;
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetDemo() {
        demoDataSeederService.resetDemoData();
        DashboardSummaryDto summary = financialEngineService.getDashboardSummary();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Demo data reset successfully for Sri Lakshmi General Stores.");
        resp.put("dashboard", summary);
        return ResponseEntity.ok(resp);
    }
}
