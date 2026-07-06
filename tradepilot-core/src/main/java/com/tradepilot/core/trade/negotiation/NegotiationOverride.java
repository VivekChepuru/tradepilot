package com.tradepilot.core.trade.negotiation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "negotiation_overrides")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String commodity;

    @Column(name = "max_auto_discount_percent", nullable = false)
    private BigDecimal maxAutoDiscountPercent;

    @Column(name = "max_escalate_discount_percent", nullable = false)
    private BigDecimal maxEscalateDiscountPercent;

    @Column(name = "is_negotiation_enabled", nullable = false)
    private Boolean isNegotiationEnabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
