package com.vyaparguard.repository;

import com.vyaparguard.model.Invoice;
import com.vyaparguard.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByBusinessIdOrderByDueDateAsc(Long businessId);

    List<Invoice> findByBusinessIdAndStatusOrderByDueDateAsc(Long businessId, InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.businessId = :businessId AND (i.status = 'OVERDUE' OR (i.status = 'PENDING' AND i.dueDate < :today)) ORDER BY i.dueDate ASC")
    List<Invoice> findOverdueInvoices(@Param("businessId") Long businessId, @Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.businessId = :businessId AND i.status != 'PAID'")
    BigDecimal sumTotalReceivables(@Param("businessId") Long businessId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Invoice i WHERE i.businessId = :businessId AND (i.status = 'OVERDUE' OR (i.status = 'PENDING' AND i.dueDate < :today))")
    BigDecimal sumOverdueReceivables(@Param("businessId") Long businessId, @Param("today") LocalDate today);

    @Query("SELECT i FROM Invoice i WHERE i.businessId = :businessId AND i.status != 'PAID' AND i.dueDate BETWEEN :startDate AND :endDate")
    List<Invoice> findPendingDueBetween(@Param("businessId") Long businessId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
