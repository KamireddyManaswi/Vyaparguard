package com.vyaparguard.service;

import com.vyaparguard.model.*;
import com.vyaparguard.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemoDataSeederService implements CommandLineRunner {

    private final BusinessRepository businessRepository;
    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentObligationRepository paymentObligationRepository;

    public DemoDataSeederService(BusinessRepository businessRepository,
                                 TransactionRepository transactionRepository,
                                 InvoiceRepository invoiceRepository,
                                 PaymentObligationRepository paymentObligationRepository) {
        this.businessRepository = businessRepository;
        this.transactionRepository = transactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentObligationRepository = paymentObligationRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (businessRepository.count() == 0) {
            seedDemoData();
        }
    }

    @Transactional
    public void resetDemoData() {
        paymentObligationRepository.deleteAll();
        invoiceRepository.deleteAll();
        transactionRepository.deleteAll();
        businessRepository.deleteAll();
        seedDemoData();
    }

    @Transactional
    public void seedDemoData() {
        LocalDate today = LocalDate.now();

        // 1. Business
        Business business = new Business(
                "Sri Lakshmi General Stores",
                new BigDecimal("120000.00"),
                new BigDecimal("50000.00")
        );
        business = businessRepository.save(business);
        Long bizId = business.getId();

        // 2. 30 Realistic Transactions over the past 30 days
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(bizId, today.minusDays(29), TransactionType.CASH_IN, new BigDecimal("12500.00"), "Daily Sales", "Counter Cash & UPI Sales"));
        transactions.add(new Transaction(bizId, today.minusDays(28), TransactionType.CASH_OUT, new BigDecimal("14200.00"), "Inventory", "Dairy & Bakery Fresh Stock"));
        transactions.add(new Transaction(bizId, today.minusDays(27), TransactionType.CASH_IN, new BigDecimal("13200.00"), "Daily Sales", "Counter Sales & PhonePe settlements"));
        transactions.add(new Transaction(bizId, today.minusDays(26), TransactionType.CASH_OUT, new BigDecimal("16500.00"), "Inventory", "Spices & Dry Goods Wholesaler"));
        transactions.add(new Transaction(bizId, today.minusDays(25), TransactionType.CASH_IN, new BigDecimal("15000.00"), "B2B Payment", "Settlement from Anand Sweets"));
        transactions.add(new Transaction(bizId, today.minusDays(24), TransactionType.CASH_OUT, new BigDecimal("5500.00"), "Logistics", "Local Delivery Auto Freight"));
        transactions.add(new Transaction(bizId, today.minusDays(23), TransactionType.CASH_IN, new BigDecimal("11800.00"), "Daily Sales", "Evening Grocery & Provisions"));
        transactions.add(new Transaction(bizId, today.minusDays(22), TransactionType.CASH_OUT, new BigDecimal("9800.00"), "Utilities", "Electricity Bill (BESCOM)"));
        transactions.add(new Transaction(bizId, today.minusDays(21), TransactionType.CASH_IN, new BigDecimal("14400.00"), "Daily Sales", "Weekend Festival Rush Counter"));
        transactions.add(new Transaction(bizId, today.minusDays(20), TransactionType.CASH_OUT, new BigDecimal("16000.00"), "Salaries", "Store Assistants Weekly Advance"));
        transactions.add(new Transaction(bizId, today.minusDays(19), TransactionType.CASH_IN, new BigDecimal("12200.00"), "Daily Sales", "UPI & Debit Card Settlements"));
        transactions.add(new Transaction(bizId, today.minusDays(18), TransactionType.CASH_OUT, new BigDecimal("18000.00"), "Inventory", "Edible Oil Bulk Cans"));
        transactions.add(new Transaction(bizId, today.minusDays(17), TransactionType.CASH_IN, new BigDecimal("13900.00"), "Daily Sales", "Counter Collections"));
        transactions.add(new Transaction(bizId, today.minusDays(16), TransactionType.CASH_OUT, new BigDecimal("8400.00"), "Maintenance", "Deep Freezer Repair & Servicing"));
        transactions.add(new Transaction(bizId, today.minusDays(15), TransactionType.CASH_IN, new BigDecimal("11500.00"), "B2B Payment", "Mehta Kirana Part Payment"));
        transactions.add(new Transaction(bizId, today.minusDays(14), TransactionType.CASH_OUT, new BigDecimal("19000.00"), "Inventory", "Rice & Wheat Flour Bags"));
        transactions.add(new Transaction(bizId, today.minusDays(13), TransactionType.CASH_IN, new BigDecimal("12100.00"), "Daily Sales", "UPI Merchant Settlement"));
        transactions.add(new Transaction(bizId, today.minusDays(12), TransactionType.CASH_OUT, new BigDecimal("6500.00"), "Packaging", "Carry bags & Billing rolls"));
        transactions.add(new Transaction(bizId, today.minusDays(11), TransactionType.CASH_IN, new BigDecimal("10800.00"), "Daily Sales", "Daily counter retail"));
        transactions.add(new Transaction(bizId, today.minusDays(10), TransactionType.CASH_OUT, new BigDecimal("14500.00"), "Inventory", "Beverages & Cold Drinks Stock"));
        transactions.add(new Transaction(bizId, today.minusDays(9), TransactionType.CASH_IN, new BigDecimal("11700.00"), "Daily Sales", "Customer walk-in receipts"));
        transactions.add(new Transaction(bizId, today.minusDays(8), TransactionType.CASH_OUT, new BigDecimal("13200.00"), "Vendor", "Snacks Distributor Settlement"));
        transactions.add(new Transaction(bizId, today.minusDays(7), TransactionType.CASH_IN, new BigDecimal("12600.00"), "Daily Sales", "Store Billing System"));
        transactions.add(new Transaction(bizId, today.minusDays(6), TransactionType.CASH_OUT, new BigDecimal("15000.00"), "Inventory", "Detergents & Household Care"));
        transactions.add(new Transaction(bizId, today.minusDays(5), TransactionType.CASH_IN, new BigDecimal("14400.00"), "Daily Sales", "Peak weekend grocery bills"));
        transactions.add(new Transaction(bizId, today.minusDays(4), TransactionType.CASH_OUT, new BigDecimal("16500.00"), "Inventory", "Confectionery & Biscuits Carts"));
        transactions.add(new Transaction(bizId, today.minusDays(3), TransactionType.CASH_IN, new BigDecimal("12200.00"), "Daily Sales", "Retail UPI Cash Inflow"));
        transactions.add(new Transaction(bizId, today.minusDays(2), TransactionType.CASH_OUT, new BigDecimal("7500.00"), "Utilities", "High-speed Internet & POS charges"));
        transactions.add(new Transaction(bizId, today.minusDays(1), TransactionType.CASH_IN, new BigDecimal("11100.00"), "Daily Sales", "Yesterday Cash & UPI tally"));
        transactions.add(new Transaction(bizId, today, TransactionType.CASH_OUT, new BigDecimal("6200.00"), "Logistics", "Morning Wholesale Market Transport"));

        transactionRepository.saveAll(transactions);

        // 3. Invoices (Customer Receivables)
        List<Invoice> invoices = new ArrayList<>();
        // Overdue customer invoices (Critical collection opportunities)
        invoices.add(new Invoice(bizId, "INV-2026-088", "ABC Traders", new BigDecimal("30000.00"), today.minusDays(22), today.minusDays(5), null, InvoiceStatus.OVERDUE));
        invoices.add(new Invoice(bizId, "INV-2026-089", "Mehta Kirana Mart", new BigDecimal("24500.00"), today.minusDays(20), today.minusDays(6), null, InvoiceStatus.OVERDUE));
        invoices.add(new Invoice(bizId, "INV-2026-092", "Anand Sweets & Snacks", new BigDecimal("18000.00"), today.minusDays(18), today.minusDays(4), null, InvoiceStatus.OVERDUE));
        invoices.add(new Invoice(bizId, "INV-2026-095", "Sri Balaji Caterers", new BigDecimal("12500.00"), today.minusDays(15), today.minusDays(2), null, InvoiceStatus.OVERDUE));

        // Pending invoices due within next 7 days or beyond
        invoices.add(new Invoice(bizId, "INV-2026-098", "Venkateshwara Super Bazaar", new BigDecimal("15000.00"), today.minusDays(7), today.plusDays(6), null, InvoiceStatus.PENDING));
        invoices.add(new Invoice(bizId, "INV-2026-101", "Ganesh Hotel Provisions", new BigDecimal("9500.00"), today.minusDays(5), today.plusDays(8), null, InvoiceStatus.PENDING));
        invoices.add(new Invoice(bizId, "INV-2026-103", "New Modern College Canteen", new BigDecimal("14000.00"), today.minusDays(3), today.plusDays(11), null, InvoiceStatus.PENDING));
        invoices.add(new Invoice(bizId, "INV-2026-105", "Sai Ram Tiffin Services", new BigDecimal("8200.00"), today.minusDays(1), today.plusDays(14), null, InvoiceStatus.PENDING));

        // Paid invoices (historical records)
        invoices.add(new Invoice(bizId, "INV-2026-075", "City Bakery & Confectionery", new BigDecimal("16000.00"), today.minusDays(35), today.minusDays(21), today.minusDays(20), InvoiceStatus.PAID));
        invoices.add(new Invoice(bizId, "INV-2026-079", "Annapurna Mess", new BigDecimal("11500.00"), today.minusDays(30), today.minusDays(16), today.minusDays(15), InvoiceStatus.PAID));
        invoices.add(new Invoice(bizId, "INV-2026-084", "Raghavendra Caterers", new BigDecimal("22000.00"), today.minusDays(25), today.minusDays(11), today.minusDays(10), InvoiceStatus.PAID));

        invoiceRepository.saveAll(invoices);

        // 4. 8 Payment Obligations (Supplier liabilities)
        List<PaymentObligation> obligations = new ArrayList<>();
        // Big upcoming supplier payments in next 7 days that trigger the cash crunch!
        obligations.add(new PaymentObligation(bizId, "ITC Distribution Hub", new BigDecimal("42000.00"), today.plusDays(2), "FMCG Inventory", PaymentStatus.PENDING));
        obligations.add(new PaymentObligation(bizId, "Hindustan FMCG Wholesalers", new BigDecimal("38000.00"), today.plusDays(4), "Packaged Goods", PaymentStatus.PENDING));
        obligations.add(new PaymentObligation(bizId, "Tata Consumer Direct", new BigDecimal("25000.00"), today.plusDays(5), "Beverages & Tea", PaymentStatus.PENDING));
        obligations.add(new PaymentObligation(bizId, "Shop Commercial Lease (Ramesh Properties)", new BigDecimal("20000.00"), today.plusDays(6), "Rent", PaymentStatus.PENDING));

        // Payments due beyond 7 days
        obligations.add(new PaymentObligation(bizId, "Amul Dairy Cold-chain", new BigDecimal("14000.00"), today.plusDays(10), "Dairy Products", PaymentStatus.PENDING));
        obligations.add(new PaymentObligation(bizId, "Britannia Agency Hub", new BigDecimal("11500.00"), today.plusDays(13), "Bakery Stock", PaymentStatus.PENDING));

        // Paid obligations (past settlement records)
        obligations.add(new PaymentObligation(bizId, "Fortune Edible Oils Depot", new BigDecimal("26000.00"), today.minusDays(12), "Edible Oils", PaymentStatus.PAID));
        obligations.add(new PaymentObligation(bizId, "State GST Monthly Settlement", new BigDecimal("18500.00"), today.minusDays(8), "Taxes", PaymentStatus.PAID));

        paymentObligationRepository.saveAll(obligations);
    }
}
