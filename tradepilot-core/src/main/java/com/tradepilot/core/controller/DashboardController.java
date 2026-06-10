package com.tradepilot.core.controller;

import com.tradepilot.core.trade.order.OrderRepository;
import com.tradepilot.core.trade.order.OrderStatus;
import com.tradepilot.core.trade.order.PaymentStatus;
import com.tradepilot.core.workflow.FollowUpJobRepository;
import com.tradepilot.core.workflow.FollowUpJobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final OrderRepository orderRepository;
    private final FollowUpJobRepository followUpJobRepository;

    @GetMapping("/stats")
    public DashboardStats getStats() {
        log.info("Fetching dashboard stats");
        return new DashboardStats(
                orderRepository.count(),
                orderRepository.countByStatus(OrderStatus.QUOTED),
                orderRepository.countByStatus(OrderStatus.CONFIRMED),
                orderRepository.countByPaymentStatus(PaymentStatus.OVERDUE),
                followUpJobRepository.countByStatus(FollowUpJobStatus.PENDING)
        );
    }
}
