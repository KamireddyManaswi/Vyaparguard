package com.vyaparguard.controller;

import com.vyaparguard.model.Invoice;
import com.vyaparguard.repository.InvoiceRepository;
import com.vyaparguard.service.FinancialEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final FinancialEngineService financialEngineService;

    public InvoiceController(InvoiceRepository invoiceRepository, FinancialEngineService financialEngineService) {
        this.invoiceRepository = invoiceRepository;
        this.financialEngineService = financialEngineService;
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        Long businessId = financialEngineService.getOrCreateDefaultBusiness().getId();
        return ResponseEntity.ok(invoiceRepository.findByBusinessIdOrderByDueDateAsc(businessId));
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Invoice>> getOverdueInvoices() {
        Long businessId = financialEngineService.getOrCreateDefaultBusiness().getId();
        return ResponseEntity.ok(invoiceRepository.findOverdueInvoices(businessId, LocalDate.now()));
    }
}
