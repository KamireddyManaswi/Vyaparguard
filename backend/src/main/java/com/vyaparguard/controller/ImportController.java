package com.vyaparguard.controller;

import com.vyaparguard.dto.CsvImportResultDto;
import com.vyaparguard.service.CsvImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
@CrossOrigin(origins = "*")
public class ImportController {

    private final CsvImportService csvImportService;

    public ImportController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<CsvImportResultDto> importTransactions(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvImportService.importTransactions(file));
    }

    @PostMapping("/invoices")
    public ResponseEntity<CsvImportResultDto> importInvoices(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvImportService.importInvoices(file));
    }

    @PostMapping("/payments")
    public ResponseEntity<CsvImportResultDto> importPayments(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(csvImportService.importPayments(file));
    }
}
