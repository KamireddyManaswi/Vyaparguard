package com.vyaparguard.controller;

import com.vyaparguard.model.PaymentObligation;
import com.vyaparguard.repository.PaymentObligationRepository;
import com.vyaparguard.service.FinancialEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentObligationRepository paymentObligationRepository;
    private final FinancialEngineService financialEngineService;

    public PaymentController(PaymentObligationRepository paymentObligationRepository,
                             FinancialEngineService financialEngineService) {
        this.paymentObligationRepository = paymentObligationRepository;
        this.financialEngineService = financialEngineService;
    }

    @GetMapping
    public ResponseEntity<List<PaymentObligation>> getAllPayments() {
        Long businessId = financialEngineService.getOrCreateDefaultBusiness().getId();
        return ResponseEntity.ok(paymentObligationRepository.findByBusinessIdOrderByDueDateAsc(businessId));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<PaymentObligation>> getUpcomingPayments() {
        Long businessId = financialEngineService.getOrCreateDefaultBusiness().getId();
        return ResponseEntity.ok(paymentObligationRepository.findUpcomingPayments(businessId, LocalDate.now()));
    }
}
