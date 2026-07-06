package com.tradepilot.core.trade.negotiation;

import com.tradepilot.core.trade.pricing.PriceCalculationService;
import com.tradepilot.core.trade.pricing.PriceQuote;
import com.tradepilot.core.webhook.dto.AiResultEvent;
import com.tradepilot.core.webhook.dto.OutboundMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NegotiationService {

    private static final BigDecimal DEFAULT_MAX_AUTO_DISCOUNT_PERCENT = BigDecimal.valueOf(2.0);
    private static final BigDecimal DEFAULT_MAX_ESCALATE_DISCOUNT_PERCENT = BigDecimal.valueOf(5.0);

    private final NegotiationSettingsRepository negotiationSettingsRepository;
    private final NegotiationOverrideRepository negotiationOverrideRepository;
    private final PriceCalculationService priceCalculationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${tradepilot.kafka.topics.messages-outbound}")
    private String outboundTopic;

    private record NegotiationThresholds(BigDecimal maxAutoPercent, BigDecimal maxEscalatePercent, boolean isEnabled) {}

    public void processNegotiation(AiResultEvent event) {
        Map<String, Object> entities = event.getExtractedEntities();
        double requestedDiscountPercent = safeDouble(entities, "discountPercent");
        String commodity = safeString(entities, "commodity");
        String grade = safeString(entities, "grade");
        String distributorName = safeString(entities, "distributorName");

        NegotiationThresholds thresholds = resolveThresholds(commodity);

        if (!thresholds.isEnabled()) {
            OutboundMessageEvent disabledOutbound = OutboundMessageEvent.builder()
                    .whatsappMessageId(event.getMessageId())
                    .fromNumber(event.getFrom())
                    .suggestedReply("Thank you for your interest. Our team will contact you with pricing options.")
                    .routingDecision("ESCALATED")
                    .commodity(commodity)
                    .grade(grade)
                    .processedAt(LocalDateTime.now())
                    .build();

            log.info("Negotiation disabled for commodity={} — escalating messageId={}", commodity, event.getMessageId());
            kafkaTemplate.send(outboundTopic, event.getFrom(), disabledOutbound);
            return;
        }

        double maxAuto = thresholds.maxAutoPercent().doubleValue();
        double maxEscalate = thresholds.maxEscalatePercent().doubleValue();

        OutboundMessageEvent outbound;

        if (requestedDiscountPercent <= maxAuto) {
            PriceQuote quote = priceCalculationService.calculateQuote(commodity, grade, null, null, distributorName);
            BigDecimal discountedPrice = quote.finalPricePerUnit()
                    .multiply(BigDecimal.valueOf(1.0 - requestedDiscountPercent / 100.0))
                    .setScale(2, RoundingMode.HALF_UP);

            String commodityGrade = grade != null ? commodity + " " + grade : commodity;
            String suggestedReply = String.format(
                    "Special offer: %s at ₹%s/%s (incl. GST). %.1f%% discount applied.",
                    commodityGrade,
                    discountedPrice.toPlainString(),
                    quote.unit(),
                    requestedDiscountPercent
            );

            outbound = OutboundMessageEvent.builder()
                    .whatsappMessageId(event.getMessageId())
                    .fromNumber(event.getFrom())
                    .suggestedReply(suggestedReply)
                    .routingDecision("PRICE_QUOTED")
                    .commodity(commodity)
                    .grade(grade)
                    .finalPricePerUnit(discountedPrice)
                    .unit(quote.unit())
                    .processedAt(LocalDateTime.now())
                    .build();

            log.info("Auto-approved negotiation: {}% for messageId={}", requestedDiscountPercent, event.getMessageId());

        } else if (requestedDiscountPercent <= maxEscalate) {
            outbound = OutboundMessageEvent.builder()
                    .whatsappMessageId(event.getMessageId())
                    .fromNumber(event.getFrom())
                    .suggestedReply(null)
                    .routingDecision("PENDING_APPROVAL")
                    .commodity(commodity)
                    .grade(grade)
                    .processedAt(LocalDateTime.now())
                    .build();

            log.info("Negotiation pending approval: {}% for messageId={}", requestedDiscountPercent, event.getMessageId());

        } else {
            outbound = OutboundMessageEvent.builder()
                    .whatsappMessageId(event.getMessageId())
                    .fromNumber(event.getFrom())
                    .suggestedReply("Thank you for your interest. Your request has been forwarded to our sales team who will contact you shortly.")
                    .routingDecision("ESCALATED")
                    .commodity(commodity)
                    .grade(grade)
                    .processedAt(LocalDateTime.now())
                    .build();

            log.info("Negotiation rejected and escalated: {}% exceeds max for messageId={}", requestedDiscountPercent, event.getMessageId());
        }

        kafkaTemplate.send(outboundTopic, event.getFrom(), outbound);
    }

    private NegotiationThresholds resolveThresholds(String commodity) {
        Optional<NegotiationOverride> overrideOpt = negotiationOverrideRepository.findByCommodityIgnoreCase(commodity);
        if (overrideOpt.isPresent()) {
            NegotiationOverride override = overrideOpt.get();
            if (Boolean.TRUE.equals(override.getIsNegotiationEnabled())) {
                return new NegotiationThresholds(
                        override.getMaxAutoDiscountPercent(),
                        override.getMaxEscalateDiscountPercent(),
                        true
                );
            }
            return new NegotiationThresholds(BigDecimal.ZERO, BigDecimal.ZERO, false);
        }

        return negotiationSettingsRepository.findTopByOrderByUpdatedAtDesc()
                .map(settings -> new NegotiationThresholds(
                        settings.getMaxAutoDiscountPercent(),
                        settings.getMaxEscalateDiscountPercent(),
                        Boolean.TRUE.equals(settings.getIsNegotiationEnabled())
                ))
                .orElseGet(() -> {
                    log.warn("No global negotiation settings found in database — using hardcoded defaults {}/{} for commodity={}",
                            DEFAULT_MAX_AUTO_DISCOUNT_PERCENT, DEFAULT_MAX_ESCALATE_DISCOUNT_PERCENT, commodity);
                    return new NegotiationThresholds(DEFAULT_MAX_AUTO_DISCOUNT_PERCENT, DEFAULT_MAX_ESCALATE_DISCOUNT_PERCENT, true);
                });
    }

    private String safeString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private double safeDouble(Map<String, Object> map, String key) {
        if (map == null) return 0.0;
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
