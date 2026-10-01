package com.vyaparguard.repository;

import com.vyaparguard.model.PaymentObligation;
import com.vyaparguard.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentObligationRepository extends JpaRepository<PaymentObligation, Long> {
    List<PaymentObligation> findByBusinessIdOrderByDueDateAsc(Long businessId);

    List<PaymentObligation> findByBusinessIdAndStatusOrderByDueDateAsc(Long businessId, PaymentStatus status);

    @Query("SELECT p FROM PaymentObligation p WHERE p.businessId = :businessId AND p.status != 'PAID' AND p.dueDate >= :today ORDER BY p.dueDate ASC")
    List<PaymentObligation> findUpcomingPayments(@Param("businessId") Long businessId, @Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentObligation p WHERE p.businessId = :businessId AND p.status != 'PAID' AND p.dueDate >= :today")
    BigDecimal sumUpcomingPayments(@Param("businessId") Long businessId, @Param("today") LocalDate today);

    @Query("SELECT p FROM PaymentObligation p WHERE p.businessId = :businessId AND p.status != 'PAID' AND p.dueDate BETWEEN :startDate AND :endDate")
    List<PaymentObligation> findPendingDueBetween(@Param("businessId") Long businessId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
