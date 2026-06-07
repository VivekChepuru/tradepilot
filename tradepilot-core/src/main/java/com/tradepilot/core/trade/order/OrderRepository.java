package com.tradepilot.core.trade.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByWhatsappThreadId(String threadId);
    List<Order> findByTradeContactIdAndStatusIn(Long contactId, List<OrderStatus> statuses);
    Optional<Order> findByOrderReference(String reference);
}
