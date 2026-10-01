package com.vyaparguard.service;

import com.vyaparguard.dto.CsvImportResultDto;
import com.vyaparguard.model.*;
import com.vyaparguard.repository.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class CsvImportService {

    private final FinancialEngineService financialEngineService;
    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentObligationRepository paymentObligationRepository;

    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    public CsvImportService(FinancialEngineService financialEngineService,
                            TransactionRepository transactionRepository,
                            InvoiceRepository invoiceRepository,
                            PaymentObligationRepository paymentObligationRepository) {
        this.financialEngineService = financialEngineService;
        this.transactionRepository = transactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentObligationRepository = paymentObligationRepository;
    }

    private LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim();
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(clean, formatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException("Invalid date format: '" + text + "'. Expected format: YYYY-MM-DD or DD-MM-YYYY");
    }

    private BigDecimal parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }
        String clean = text.replaceAll("[,\u20B9\\s]", "");
        try {
            BigDecimal amt = new BigDecimal(clean);
            if (amt.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Amount must be a positive number: " + text);
            }
            return amt;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric amount: '" + text + "'");
        }
    }

    private String getHeaderValue(CSVRecord record, String... possibleHeaders) {
        for (String h : possibleHeaders) {
            if (record.isMapped(h)) {
                String val = record.get(h);
                if (val != null && !val.trim().isEmpty()) {
                    return val.trim();
                }
            }
            // Case-insensitive check
            for (String key : record.toMap().keySet()) {
                if (key.trim().equalsIgnoreCase(h.trim())) {
                    String val = record.get(key);
                    if (val != null && !val.trim().isEmpty()) {
                        return val.trim();
                    }
                }
            }
        }
        return null;
    }

    @Transactional
    public CsvImportResultDto importTransactions(MultipartFile file) {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        List<String> errors = new ArrayList<>();
        List<Transaction> validTransactions = new ArrayList<>();
        int lineNum = 1;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                lineNum++;
                try {
                    String dateStr = getHeaderValue(record, "date", "txn_date", "transaction_date");
                    if (dateStr == null) throw new IllegalArgumentException("Missing required column 'date'");
                    LocalDate date = parseDate(dateStr);

                    String typeStr = getHeaderValue(record, "type", "txn_type", "transaction_type");
                    if (typeStr == null) throw new IllegalArgumentException("Missing required column 'type'");
                    TransactionType type;
                    String upperType = typeStr.toUpperCase();
                    if (upperType.contains("IN") || upperType.equals("CREDIT")) {
                        type = TransactionType.CASH_IN;
                    } else if (upperType.contains("OUT") || upperType.equals("DEBIT")) {
                        type = TransactionType.CASH_OUT;
                    } else {
                        throw new IllegalArgumentException("Invalid type '" + typeStr + "'. Expected CASH_IN or CASH_OUT");
                    }

                    String amountStr = getHeaderValue(record, "amount", "amt", "value");
                    if (amountStr == null) throw new IllegalArgumentException("Missing required column 'amount'");
                    BigDecimal amount = parseAmount(amountStr);

                    String category = getHeaderValue(record, "category", "cat");
                    if (category == null) category = "General";

                    String desc = getHeaderValue(record, "description", "desc", "particulars");
                    if (desc == null) desc = "Imported transaction";

                    validTransactions.add(new Transaction(business.getId(), date, type, amount, category, desc));
                } catch (Exception e) {
                    errors.add("Row " + lineNum + ": " + e.getMessage());
                }
            }

            if (!validTransactions.isEmpty()) {
                transactionRepository.saveAll(validTransactions);
            }

            return new CsvImportResultDto(
                    errors.isEmpty() || !validTransactions.isEmpty(),
                    validTransactions.size(),
                    errors.size(),
                    errors,
                    String.format("Successfully imported %d transactions with %d validation errors.", validTransactions.size(), errors.size())
            );

        } catch (Exception e) {
            return new CsvImportResultDto(false, 0, 1, Collections.singletonList("Failed to process CSV file: " + e.getMessage()), "CSV file parsing error.");
        }
    }

    @Transactional
    public CsvImportResultDto importInvoices(MultipartFile file) {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        List<String> errors = new ArrayList<>();
        List<Invoice> validInvoices = new ArrayList<>();
        int lineNum = 1;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                lineNum++;
                try {
                    String invNum = getHeaderValue(record, "invoiceNumber", "invoice_number", "inv_num", "number");
                    if (invNum == null) throw new IllegalArgumentException("Missing required column 'invoiceNumber'");

                    String customer = getHeaderValue(record, "customerName", "customer_name", "customer", "party");
                    if (customer == null) throw new IllegalArgumentException("Missing required column 'customerName'");

                    String amountStr = getHeaderValue(record, "amount", "amt", "total");
                    if (amountStr == null) throw new IllegalArgumentException("Missing required column 'amount'");
                    BigDecimal amount = parseAmount(amountStr);

                    String issueDateStr = getHeaderValue(record, "issueDate", "issue_date", "date");
                    LocalDate issueDate = issueDateStr != null ? parseDate(issueDateStr) : LocalDate.now();

                    String dueDateStr = getHeaderValue(record, "dueDate", "due_date");
                    if (dueDateStr == null) throw new IllegalArgumentException("Missing required column 'dueDate'");
                    LocalDate dueDate = parseDate(dueDateStr);

                    String paidDateStr = getHeaderValue(record, "paidDate", "paid_date");
                    LocalDate paidDate = paidDateStr != null ? parseDate(paidDateStr) : null;

                    String statusStr = getHeaderValue(record, "status");
                    InvoiceStatus status = InvoiceStatus.PENDING;
                    if (statusStr != null) {
                        try {
                            status = InvoiceStatus.valueOf(statusStr.toUpperCase());
                        } catch (Exception ex) {
                            if (paidDate != null) status = InvoiceStatus.PAID;
                            else if (dueDate.isBefore(LocalDate.now())) status = InvoiceStatus.OVERDUE;
                        }
                    } else {
                        if (dueDate.isBefore(LocalDate.now())) status = InvoiceStatus.OVERDUE;
                    }

                    validInvoices.add(new Invoice(business.getId(), invNum, customer, amount, issueDate, dueDate, paidDate, status));
                } catch (Exception e) {
                    errors.add("Row " + lineNum + ": " + e.getMessage());
                }
            }

            if (!validInvoices.isEmpty()) {
                invoiceRepository.saveAll(validInvoices);
            }

            return new CsvImportResultDto(
                    errors.isEmpty() || !validInvoices.isEmpty(),
                    validInvoices.size(),
                    errors.size(),
                    errors,
                    String.format("Successfully imported %d invoices with %d validation errors.", validInvoices.size(), errors.size())
            );

        } catch (Exception e) {
            return new CsvImportResultDto(false, 0, 1, Collections.singletonList("Failed to process CSV file: " + e.getMessage()), "CSV file parsing error.");
        }
    }

    @Transactional
    public CsvImportResultDto importPayments(MultipartFile file) {
        Business business = financialEngineService.getOrCreateDefaultBusiness();
        List<String> errors = new ArrayList<>();
        List<PaymentObligation> validPayments = new ArrayList<>();
        int lineNum = 1;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                lineNum++;
                try {
                    String supplier = getHeaderValue(record, "supplierName", "supplier_name", "supplier", "vendor");
                    if (supplier == null) throw new IllegalArgumentException("Missing required column 'supplierName'");

                    String amountStr = getHeaderValue(record, "amount", "amt");
                    if (amountStr == null) throw new IllegalArgumentException("Missing required column 'amount'");
                    BigDecimal amount = parseAmount(amountStr);

                    String dueDateStr = getHeaderValue(record, "dueDate", "due_date");
                    if (dueDateStr == null) throw new IllegalArgumentException("Missing required column 'dueDate'");
                    LocalDate dueDate = parseDate(dueDateStr);

                    String category = getHeaderValue(record, "category", "cat");
                    if (category == null) category = "General Inventory";

                    String statusStr = getHeaderValue(record, "status");
                    PaymentStatus status = PaymentStatus.PENDING;
                    if (statusStr != null) {
                        try {
                            status = PaymentStatus.valueOf(statusStr.toUpperCase());
                        } catch (Exception ex) {
                            if (dueDate.isBefore(LocalDate.now())) status = PaymentStatus.OVERDUE;
                        }
                    }

                    validPayments.add(new PaymentObligation(business.getId(), supplier, amount, dueDate, category, status));
                } catch (Exception e) {
                    errors.add("Row " + lineNum + ": " + e.getMessage());
                }
            }

            if (!validPayments.isEmpty()) {
                paymentObligationRepository.saveAll(validPayments);
            }

            return new CsvImportResultDto(
                    errors.isEmpty() || !validPayments.isEmpty(),
                    validPayments.size(),
                    errors.size(),
                    errors,
                    String.format("Successfully imported %d payments with %d validation errors.", validPayments.size(), errors.size())
            );

        } catch (Exception e) {
            return new CsvImportResultDto(false, 0, 1, Collections.singletonList("Failed to process CSV file: " + e.getMessage()), "CSV file parsing error.");
        }
    }
}
