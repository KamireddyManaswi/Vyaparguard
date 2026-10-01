package com.vyaparguard.controller;

import com.vyaparguard.dto.AiChatRequestDto;
import com.vyaparguard.dto.AiChatResponseDto;
import com.vyaparguard.service.AiAdvisorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiController {

    private final AiAdvisorService aiAdvisorService;

    public AiController(AiAdvisorService aiAdvisorService) {
        this.aiAdvisorService = aiAdvisorService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AiChatResponseDto> askQuestion(@RequestBody AiChatRequestDto request) {
        return ResponseEntity.ok(aiAdvisorService.answerQuestion(request));
    }
}
