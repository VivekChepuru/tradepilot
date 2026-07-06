package com.tradepilot.core.trade.negotiation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "negotiation_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NegotiationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "max_auto_discount_percent", nullable = false)
    private BigDecimal maxAutoDiscountPercent;

    @Column(name = "max_escalate_discount_percent", nullable = false)
    private BigDecimal maxEscalateDiscountPercent;

    @Column(name = "is_negotiation_enabled", nullable = false)
    private Boolean isNegotiationEnabled;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
