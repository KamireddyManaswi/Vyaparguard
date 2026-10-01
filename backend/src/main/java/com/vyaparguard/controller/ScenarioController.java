package com.vyaparguard.controller;

import com.vyaparguard.dto.ScenarioSimulationRequestDto;
import com.vyaparguard.dto.ScenarioSimulationResponseDto;
import com.vyaparguard.service.FinancialEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scenarios")
@CrossOrigin(origins = "*")
public class ScenarioController {

    private final FinancialEngineService financialEngineService;

    public ScenarioController(FinancialEngineService financialEngineService) {
        this.financialEngineService = financialEngineService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<ScenarioSimulationResponseDto> simulate(@RequestBody ScenarioSimulationRequestDto request) {
        return ResponseEntity.ok(financialEngineService.simulateScenario(request));
    }
}
