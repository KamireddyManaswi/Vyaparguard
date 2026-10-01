package com.vyaparguard.repository;

import com.vyaparguard.model.ProposedAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposedActionRepository extends JpaRepository<ProposedAction, Long> {
    List<ProposedAction> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
}
