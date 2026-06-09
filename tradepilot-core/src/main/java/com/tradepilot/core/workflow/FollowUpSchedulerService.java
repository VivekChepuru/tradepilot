package com.tradepilot.core.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradepilot.core.trade.customer.TradeContact;
import com.tradepilot.core.trade.order.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpSchedulerService {

    private final FollowUpJobRepository followUpJobRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void scheduleInquiryFollowUps(Order order, TradeContact contact) {
        LocalDateTime now = LocalDateTime.now();
        String payload = buildPayload(order, contact);

        List<FollowUpJob> jobs = List.of(
                buildJob(order, contact, FollowUpJobType.INQUIRY_FOLLOWUP, now.plusMinutes(90),  "INQUIRY_FOLLOWUP_1", payload),
                buildJob(order, contact, FollowUpJobType.INQUIRY_FOLLOWUP, now.plusHours(4),     "INQUIRY_FOLLOWUP_2", payload),
                buildJob(order, contact, FollowUpJobType.INQUIRY_FOLLOWUP, now.plusHours(24),    "INQUIRY_FOLLOWUP_3", payload)
        );

        followUpJobRepository.saveAll(jobs);
        log.info("Scheduled {} inquiry follow-up jobs for orderId={} contact={}",
                jobs.size(), order.getId(), contact.getWhatsappNumber());
    }

    @Transactional
    public void schedulePaymentReminders(Order order, TradeContact contact) {
        LocalDateTime now = LocalDateTime.now();
        String payload = buildPayload(order, contact);

        List<FollowUpJob> jobs = List.of(
                buildJob(order, contact, FollowUpJobType.PAYMENT_REMINDER, now,              "PAYMENT_REMINDER_DUE", payload),
                buildJob(order, contact, FollowUpJobType.PAYMENT_REMINDER, now.plusDays(3),  "PAYMENT_REMINDER_3D",  payload),
                buildJob(order, contact, FollowUpJobType.PAYMENT_REMINDER, now.plusDays(7),  "PAYMENT_REMINDER_7D",  payload),
                buildJob(order, contact, FollowUpJobType.PAYMENT_REMINDER, now.plusDays(15), "PAYMENT_REMINDER_15D", payload)
        );

        followUpJobRepository.saveAll(jobs);
        log.info("Scheduled {} payment reminder jobs for orderId={} contact={}",
                jobs.size(), order.getId(), contact.getWhatsappNumber());
    }

    @Transactional
    public void cancelPendingFollowUps(Long orderId) {
        List<FollowUpJob> pending = followUpJobRepository
                .findByOrderIdAndJobTypeAndStatus(orderId, FollowUpJobType.INQUIRY_FOLLOWUP, FollowUpJobStatus.PENDING);

        pending.forEach(job -> job.setStatus(FollowUpJobStatus.CANCELLED));
        followUpJobRepository.saveAll(pending);
        log.info("Cancelled {} follow-up jobs for orderId={}", pending.size(), orderId);
    }

    private FollowUpJob buildJob(Order order, TradeContact contact, FollowUpJobType type,
                                 LocalDateTime scheduledAt, String template, String payload) {
        return FollowUpJob.builder()
                .jobType(type)
                .order(order)
                .tradeContact(contact)
                .scheduledAt(scheduledAt)
                .status(FollowUpJobStatus.PENDING)
                .attemptCount(0)
                .messageTemplate(template)
                .contextPayload(payload)
                .build();
    }

    private String buildPayload(Order order, TradeContact contact) {
        Map<String, String> payload = Map.of(
                "orderReference", order.getOrderReference(),
                "toNumber",       contact.getWhatsappNumber(),
                "commodity",      order.getCommodity() != null ? order.getCommodity() : "",
                "grade",          order.getGrade()     != null ? order.getGrade()     : ""
        );
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize context payload for order {}", order.getId(), e);
            return "{}";
        }
    }
}
