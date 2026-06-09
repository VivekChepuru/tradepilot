package com.tradepilot.core.trade.order;

import com.tradepilot.core.trade.customer.TradeContact;
import com.tradepilot.core.trade.customer.TradeContactRepository;
import com.tradepilot.core.webhook.dto.OutboundMessageEvent;
import com.tradepilot.core.workflow.FollowUpSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final List<OrderStatus> OPEN_STATUSES =
            List.of(OrderStatus.INQUIRY, OrderStatus.QUOTED, OrderStatus.NEGOTIATING);

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS;

    static {
        ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);
        ALLOWED_TRANSITIONS.put(OrderStatus.INQUIRY,     EnumSet.of(OrderStatus.QUOTED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.QUOTED,      EnumSet.of(OrderStatus.NEGOTIATING, OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.NEGOTIATING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED,   EnumSet.of(OrderStatus.DISPATCHED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DISPATCHED,  EnumSet.of(OrderStatus.DELIVERED));
        ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED,   EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED,   EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final TradeContactRepository tradeContactRepository;
    private final FollowUpSchedulerService followUpSchedulerService;

    @Transactional
    public Order createOrUpdateFromQuote(OutboundMessageEvent event) {
        TradeContact contact = tradeContactRepository.findByWhatsappNumber(event.getFromNumber())
                .orElseGet(() -> {
                    TradeContact newContact = TradeContact.builder()
                            .whatsappNumber(event.getFromNumber())
                            .displayName(event.getFromNumber())
                            .contactType("BUYER")
                            .isActive(true)
                            .build();
                    return tradeContactRepository.save(newContact);
                });

        List<Order> openOrders = orderRepository.findByTradeContactIdAndStatusIn(contact.getId(), OPEN_STATUSES);

        if (!openOrders.isEmpty()) {
            Order existing = openOrders.get(0);
            existing.setCommodity(event.getCommodity());
            existing.setGrade(event.getGrade());
            existing.setQuotedPrice(event.getFinalPricePerUnit());
            existing.setTotalAmount(event.getTotalAmount());
            existing.setUnit(event.getUnit());
            existing.setStatus(OrderStatus.QUOTED);
            return orderRepository.save(existing);
        }

        Order newOrder = Order.builder()
                .orderReference("TP-" + System.currentTimeMillis())
                .tradeContact(contact)
                .whatsappThreadId(event.getWhatsappMessageId())
                .commodity(event.getCommodity())
                .grade(event.getGrade())
                .quotedPrice(event.getFinalPricePerUnit())
                .totalAmount(event.getTotalAmount())
                .unit(event.getUnit())
                .status(OrderStatus.QUOTED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        Order saved = orderRepository.save(newOrder);
        followUpSchedulerService.scheduleInquiryFollowUps(saved, contact);
        log.info("Scheduled inquiry follow-ups for orderId={} contact={}", saved.getId(), contact.getWhatsappNumber());
        return saved;
    }

    @Transactional
    public Order transitionStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), EnumSet.noneOf(OrderStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(String.format(
                    "Invalid transition from %s to %s for order %d", order.getStatus(), newStatus, orderId));
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public List<Order> getOpenOrdersForContact(String whatsappNumber) {
        TradeContact contact = tradeContactRepository.findByWhatsappNumber(whatsappNumber)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + whatsappNumber));
        return orderRepository.findByTradeContactIdAndStatusIn(contact.getId(), OPEN_STATUSES);
    }
}
