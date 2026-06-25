package com.tradepilot.core.controller;

import com.tradepilot.core.trade.order.Order;
import com.tradepilot.core.trade.order.OrderRepository;
import com.tradepilot.core.trade.order.OrderService;
import com.tradepilot.core.trade.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        try {
            return orderService.transitionStatus(id, request.status());
        } catch (IllegalStateException e) {
            log.warn("Invalid status transition: orderId={} requestedStatus={} reason={}", id, request.status(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Order not found: orderId={}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    record StatusUpdateRequest(OrderStatus status) {}
}
