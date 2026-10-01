package com.vyaparguard.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal openingCash;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal minimumCashThreshold;

    public Business() {
    }

    public Business(String name, BigDecimal openingCash, BigDecimal minimumCashThreshold) {
        this.name = name;
        this.openingCash = openingCash;
        this.minimumCashThreshold = minimumCashThreshold;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getOpeningCash() {
        return openingCash;
    }

    public void setOpeningCash(BigDecimal openingCash) {
        this.openingCash = openingCash;
    }

    public BigDecimal getMinimumCashThreshold() {
        return minimumCashThreshold;
    }

    public void setMinimumCashThreshold(BigDecimal minimumCashThreshold) {
        this.minimumCashThreshold = minimumCashThreshold;
    }
}
