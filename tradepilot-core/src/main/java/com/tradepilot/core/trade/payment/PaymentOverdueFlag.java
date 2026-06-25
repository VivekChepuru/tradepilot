package com.tradepilot.core.trade.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tradepilot.core.trade.customer.TradeContact;
import com.tradepilot.core.trade.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "payment_overdue_flags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOverdueFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_contact_id", nullable = false)
    private TradeContact tradeContact;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "last_manual_reminder_at")
    private LocalDateTime lastManualReminderAt;

    @Column(name = "last_reminder_tone", length = 30)
    private String lastReminderTone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (flaggedAt == null) flaggedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
