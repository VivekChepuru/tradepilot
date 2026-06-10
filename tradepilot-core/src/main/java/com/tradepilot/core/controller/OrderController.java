package com.tradepilot.core.controller;

import com.tradepilot.core.trade.order.Order;
import com.tradepilot.core.trade.order.OrderRepository;
import com.tradepilot.core.trade.order.OrderService;
import com.tradepilot.core.trade.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @GetMapping
    public List<Order> getOrders(@RequestParam(required = false) OrderStatus status) {
        if (status != null) {
            log.info("Fetching orders with status={}", status);
            return orderRepository.findByStatusOrderByCreatedAtDesc(status);
        }
        log.info("Fetching all orders");
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @PatchMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        log.info("Updating order id={} to status={}", id, request.status());
        return orderService.transitionStatus(id, request.status());
    }

    record StatusUpdateRequest(OrderStatus status) {}
}
