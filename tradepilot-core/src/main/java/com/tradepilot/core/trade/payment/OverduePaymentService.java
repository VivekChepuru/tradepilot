package com.tradepilot.core.trade.payment;

import com.tradepilot.core.channel.WhatsAppSenderService;
import com.tradepilot.core.trade.customer.TradeContactRepository;
import com.tradepilot.core.trade.order.Order;
import com.tradepilot.core.trade.order.OrderRepository;
import com.tradepilot.core.trade.order.OrderStatus;
import com.tradepilot.core.trade.order.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OverduePaymentService {

    private final OrderRepository orderRepository;
    private final TradeContactRepository tradeContactRepository;
    private final PaymentOverdueFlagRepository paymentOverdueFlagRepository;
    private final WhatsAppSenderService whatsAppSenderService;

    public List<PaymentOverdueFlag> getOpenFlags() {
        log.info("Fetching open overdue payment flags");
        return paymentOverdueFlagRepository.findByStatusOrderByFlaggedAtDesc("OPEN");
    }

    @Transactional
    public void flagOverdueOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<Order> confirmedOverdue = orderRepository
                .findByStatusAndPaymentStatusAndUpdatedAtBefore(OrderStatus.CONFIRMED, PaymentStatus.PENDING, cutoff);
        List<Order> quotedOverdue = orderRepository
                .findByStatusAndPaymentStatusAndCreatedAtBefore(OrderStatus.QUOTED, PaymentStatus.PENDING, cutoff);

        for (Order order : confirmedOverdue) {
            flagOrderIfNeeded(order);
        }
        for (Order order : quotedOverdue) {
            flagOrderIfNeeded(order);
        }
    }

    @Transactional
    public void sendManualReminder(Long flagId, String tone) {
        PaymentOverdueFlag flag = paymentOverdueFlagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("PaymentOverdueFlag not found: " + flagId));

        Order order = flag.getOrder();
        String message = buildReminderMessage(tone, order);

        String toNumber = order.getTradeContact().getWhatsappNumber();
        whatsAppSenderService.send(toNumber, message);

        flag.setLastManualReminderAt(LocalDateTime.now());
        flag.setLastReminderTone(tone);
        paymentOverdueFlagRepository.save(flag);

        log.info("Manual reminder sent — flagId={} tone={} to={}", flagId, tone, toNumber);
    }

    @Transactional
    public void markAsPaid(Long flagId) {
        PaymentOverdueFlag flag = paymentOverdueFlagRepository.findById(flagId)
                .orElseThrow(() -> new IllegalArgumentException("PaymentOverdueFlag not found: " + flagId));

        flag.setStatus("RESOLVED");
        flag.setResolvedAt(LocalDateTime.now());
        paymentOverdueFlagRepository.save(flag);

        Order order = flag.getOrder();
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        log.info("Order {} marked as PAID — flag {} resolved", order.getId(), flagId);
    }

    private void flagOrderIfNeeded(Order order) {
        if (paymentOverdueFlagRepository.existsByOrderIdAndStatus(order.getId(), "OPEN")) {
            return;
        }

        PaymentOverdueFlag flag = PaymentOverdueFlag.builder()
                .order(order)
                .tradeContact(order.getTradeContact())
                .flaggedAt(LocalDateTime.now())
                .status("OPEN")
                .build();
        paymentOverdueFlagRepository.save(flag);

        order.setPaymentStatus(PaymentStatus.OVERDUE);
        orderRepository.save(order);

        log.info("Flagged order {} as OVERDUE for contact {}", order.getId(), order.getTradeContact().getId());
    }

    private String buildReminderMessage(String tone, Order order) {
        String name = order.getTradeContact().getDisplayName();
        String ref = order.getOrderReference();
        String amount = order.getTotalAmount() != null ? order.getTotalAmount().toPlainString() : "0.00";

        return switch (tone) {
            case "POLITE" -> String.format(
                    "Dear %s, we noticed your payment for order %s (₹%s) is pending. " +
                    "Please arrange payment at your earliest convenience. Thank you.",
                    name, ref, amount);
            case "FIRM" -> String.format(
                    "Important: Payment for order %s (₹%s) is overdue. " +
                    "Please settle immediately to avoid service interruption.",
                    ref, amount);
            case "LEGAL_WARNING" -> String.format(
                    "FINAL NOTICE: Payment of ₹%s for order %s is seriously overdue. " +
                    "Failure to pay within 48 hours may result in legal action.",
                    amount, ref);
            default -> {
                log.warn("Unknown reminder tone '{}' — defaulting to POLITE", tone);
                yield String.format(
                        "Dear %s, we noticed your payment for order %s (₹%s) is pending. " +
                        "Please arrange payment at your earliest convenience. Thank you.",
                        name, ref, amount);
            }
        };
    }
}
