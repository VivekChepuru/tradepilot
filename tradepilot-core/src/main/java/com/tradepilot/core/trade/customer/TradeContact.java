package com.tradepilot.core.trade.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "trade_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "whatsapp_number", nullable = false, unique = true, length = 20)
    private String whatsappNumber;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "contact_type", nullable = false, length = 50)
    private String contactType;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "commodity_interest")
    private String commodityInterest;

    @Column(name = "lifetime_value", precision = 15, scale = 2)
    private BigDecimal lifetimeValue;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
