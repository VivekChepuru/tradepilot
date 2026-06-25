package com.tradepilot.core.trade.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverduePaymentScheduler {

    private final OverduePaymentService overduePaymentService;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkOverduePayments() {
        try {
            overduePaymentService.flagOverdueOrders();
            log.info("Overdue payment check completed");
        } catch (Exception e) {
            log.error("Overdue payment check failed", e);
        }
    }
}
