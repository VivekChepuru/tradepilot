package com.tradepilot.core.kafka;

import com.tradepilot.core.exception.PriceRuleNotFoundException;
import com.tradepilot.core.trade.pricing.PriceCalculationService;
import com.tradepilot.core.trade.pricing.PriceQuote;
import com.tradepilot.core.webhook.dto.AiResultEvent;
import com.tradepilot.core.webhook.dto.OutboundMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiResultsConsumer {

    private static final String PRICE_INQUIRY = "price_inquiry";
    private static final double MIN_CONFIDENCE = 0.50;

    // Multi-word prefixes must come before their single-word components
    private static final List<String> KNOWN_COMMODITY_PREFIXES = List.of(
            "MS Angle", "MS Pipe", "MS Flat", "MS Round",
            "TMT", "HRC", "DAP", "Angle", "Channel"
    );

    private final PriceCalculationService priceCalculationService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${tradepilot.kafka.topics.messages-outbound}")
    private String outboundTopic;

    @KafkaListener(
            topics = "${tradepilot.kafka.topics.ai-results}",
            groupId = "tradepilot-price-engine",
            containerFactory = "aiResultListenerContainerFactory"
    )
    public void consume(AiResultEvent event) {
        if (!PRICE_INQUIRY.equals(event.getDetectedIntent())) {
            log.info("Skipping intent={} for messageId={}", event.getDetectedIntent(), event.getMessageId());
            return;
        }

        if (event.getConfidence() == null || event.getConfidence() < MIN_CONFIDENCE) {
            log.info("Confidence {} below threshold for messageId={}", event.getConfidence(), event.getMessageId());
            return;
        }

        Map<String, Object> entities = event.getExtractedEntities();
        String commodity = safeString(entities, "commodity");
        String grade = safeString(entities, "grade");
        Object quantityRaw = entities != null ? entities.get("quantity") : null;
        Double quantity = quantityRaw != null ? Double.parseDouble(quantityRaw.toString()) : null;

        String[] normalized = normalizeCommodityGrade(commodity, grade);
        commodity = normalized[0];
        grade = normalized[1];
        log.info("Normalized commodity={}, grade={} for messageId={}", commodity, grade, event.getMessageId());

        OutboundMessageEvent outbound;
        try {
            PriceQuote quote = priceCalculationService.calculateQuote(commodity, grade, quantity, null);

            String commodityGrade = grade != null ? commodity + " " + grade : commodity;
            String suggestedReply = String.format(
                    "%s rate: ₹%s/%s (incl. GST). Valid today. Freight included.",
                    commodityGrade,
                    quote.finalPricePerUnit().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    quote.unit()
            );

            outbound = OutboundMessageEvent.builder()
                    .whatsappMessageId(event.getMessageId())
                    .fromNumber(event.getFrom())
                    .suggestedReply(suggestedReply)
                    .routingDecision("PRICE_QUOTED")
                    .commodity(quote.commodity())
                    .grade(quote.grade())
                    .finalPricePerUnit(quote.finalPricePerUnit())
                    .totalAmount(quote.totalAmount())
                    .unit(quote.unit())
                    .processedAt(LocalDateTime.now())
                    .build();

        } catch (PriceRuleNotFoundException e) {
            log.warn("No price rule found for commodity={}, grade={}, messageId={}", commodity, grade, event.getMessageId());

            outbound = OutboundMessageEvent.builder()
                    .whatsappMessageId(event.getMessageId())
                    .fromNumber(event.getFrom())
                    .suggestedReply(null)
                    .routingDecision("ESCALATED")
                    .commodity(commodity)
                    .grade(grade)
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        kafkaTemplate.send(outboundTopic, event.getFrom(), outbound);
        log.info("Published outbound event routingDecision={} for messageId={}", outbound.getRoutingDecision(), event.getMessageId());
    }

    private String[] normalizeCommodityGrade(String commodity, String grade) {
        if (commodity == null) return new String[]{null, grade};
        for (String prefix : KNOWN_COMMODITY_PREFIXES) {
            if (commodity.equalsIgnoreCase(prefix)) {
                return new String[]{prefix, grade};
            }
            if (commodity.toLowerCase().startsWith(prefix.toLowerCase() + " ")) {
                String extractedGrade = commodity.substring(prefix.length()).trim();
                return new String[]{prefix, grade != null ? grade : extractedGrade};
            }
        }
        return new String[]{commodity, grade};
    }

    private String safeString(Map<String, Object> map, String key) {
        if (map == null) return null;
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}