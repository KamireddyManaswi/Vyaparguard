package com.vyaparguard.repository;

import com.vyaparguard.model.Transaction;
import com.vyaparguard.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByBusinessIdOrderByDateDesc(Long businessId);

    List<Transaction> findByBusinessIdAndDateBetweenOrderByDateAsc(Long businessId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.businessId = :businessId AND t.type = :type")
    BigDecimal sumAmountByBusinessIdAndType(@Param("businessId") Long businessId, @Param("type") TransactionType type);
}
