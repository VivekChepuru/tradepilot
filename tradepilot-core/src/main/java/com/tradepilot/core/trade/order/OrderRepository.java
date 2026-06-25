package com.tradepilot.core.trade.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByWhatsappThreadId(String threadId);
    List<Order> findByTradeContactIdAndStatusIn(Long contactId, List<OrderStatus> statuses);
    Optional<Order> findByOrderReference(String reference);

    @EntityGraph(attributePaths = "tradeContact")
    List<Order> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "tradeContact")
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    long countByStatus(OrderStatus status);
    long countByPaymentStatus(PaymentStatus paymentStatus);

    List<Order> findByStatusAndPaymentStatusAndUpdatedAtBefore(OrderStatus status, PaymentStatus paymentStatus, LocalDateTime cutoff);
    List<Order> findByStatusAndPaymentStatusAndCreatedAtBefore(OrderStatus status, PaymentStatus paymentStatus, LocalDateTime cutoff);
}
