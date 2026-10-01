package com.vyaparguard.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "proposed_actions")
public class ProposedAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long businessId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String priority; // HIGH, MEDIUM, LOW

    private String category; // RECEIVABLES, PAYABLES, EXPENSES, GENERAL

    @Column(nullable = false)
    private String status; // PROPOSED, APPROVED, REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public ProposedAction() {
    }

    public ProposedAction(Long businessId, String title, String description, String priority, String category, String status) {
        this.businessId = businessId;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
