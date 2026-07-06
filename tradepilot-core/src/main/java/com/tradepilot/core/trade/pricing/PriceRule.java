package com.tradepilot.core.trade.pricing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String commodity;

    private String grade;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private BigDecimal marginPercent;

    @Column(nullable = false)
    private BigDecimal gstPercent;

    @Column(nullable = false)
    private BigDecimal freightPerUnit;

    @Column(nullable = false)
    private String unit;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    private String lastUpdatedBy;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column(name = "distributor_name")
    private String distributorName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}