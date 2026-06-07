package com.tradepilot.core.kafka;

import com.tradepilot.core.channel.SendResult;
import com.tradepilot.core.channel.WhatsAppSenderService;
import com.tradepilot.core.webhook.dto.OutboundMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundMessageConsumer {

    private final WhatsAppSenderService whatsAppSenderService;

    @KafkaListener(
            topics = "${tradepilot.kafka.topics.messages-outbound}",
            groupId = "tradepilot-outbound-router",
            containerFactory = "outboundListenerContainerFactory"
    )
    public void consume(OutboundMessageEvent event) {
        switch (event.getRoutingDecision()) {
            case "PRICE_QUOTED" -> {
                SendResult result = whatsAppSenderService.send(event.getFromNumber(), event.getSuggestedReply());
                if (result.success()) {
                    log.info("WhatsApp send succeeded — messageId: {} to: {}", result.messageId(), event.getFromNumber());
                    log.info("Sent reply: {}", event.getSuggestedReply());
                } else {
                    log.error("WhatsApp send failed — to: {} reason: {}", event.getFromNumber(), result.errorReason());
                }
            }
            case "PENDING_APPROVAL" ->
                log.info("Message queued for operator approval — messageId: {} to: {}",
                        event.getWhatsappMessageId(), event.getFromNumber());
            case "ESCALATED" ->
                log.info("Message escalated to human — messageId: {} reason: no price rule or low confidence",
                        event.getWhatsappMessageId());
            default ->
                log.warn("Unknown routing decision: {} for messageId: {}",
                        event.getRoutingDecision(), event.getWhatsappMessageId());
        }
    }
}