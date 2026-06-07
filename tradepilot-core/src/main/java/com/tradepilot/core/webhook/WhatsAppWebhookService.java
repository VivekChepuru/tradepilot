package com.tradepilot.core.webhook;

import com.tradepilot.core.webhook.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppWebhookService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${tradepilot.kafka.topics.messages-inbound}")
    private String messagesInboundTopic;

    public void processPayload(WhatsAppWebhookPayload payload) {
        if (payload.getEntry() == null) {
            return;
        }

        for (WhatsAppEntry entry : payload.getEntry()) {
            if (entry.getChanges() == null) continue;

            for (WhatsAppChange change : entry.getChanges()) {
                WhatsAppValue value = change.getValue();
                if (value == null || value.getMessages() == null) continue;

                Map<String, String> senderNames = buildSenderNameMap(value.getContacts());

                for (WhatsAppMessage message : value.getMessages()) {
                    InboundMessageEvent event = toEvent(message, value.getMetadata(), senderNames);
                    try {
                        kafkaTemplate.send(messagesInboundTopic, message.getFrom(), event);
                        log.info("Published InboundMessageEvent to {} for messageId={}", messagesInboundTopic, message.getId());
                    } catch (Exception e) {
                        log.error("Failed to publish InboundMessageEvent for messageId={}: {}", message.getId(), e.getMessage(), e);
                    }
                }
            }
        }
    }

    private Map<String, String> buildSenderNameMap(List<WhatsAppContact> contacts) {
        if (contacts == null) return Collections.emptyMap();
        return contacts.stream()
                .filter(c -> c.getWaId() != null && c.getProfile() != null)
                .collect(Collectors.toMap(WhatsAppContact::getWaId, c -> c.getProfile().getName()));
    }

    private InboundMessageEvent toEvent(WhatsAppMessage message, WhatsAppMetadata metadata, Map<String, String> senderNames) {
        InboundMessageEvent.InboundMessageEventBuilder builder = InboundMessageEvent.builder()
                .messageId(message.getId())
                .from(message.getFrom())
                .senderName(senderNames.get(message.getFrom()))
                .timestamp(message.getTimestamp())
                .messageType(message.getType());

        if (metadata != null) {
            builder.phoneNumberId(metadata.getPhoneNumberId())
                   .displayPhoneNumber(metadata.getDisplayPhoneNumber());
        }

        if (message.getText() != null) {
            builder.textBody(message.getText().getBody());
        }

        WhatsAppMediaBody media = resolveMedia(message);
        if (media != null) {
            builder.mediaId(media.getId())
                   .mediaCaption(media.getCaption());
        }

        return builder.build();
    }

    private WhatsAppMediaBody resolveMedia(WhatsAppMessage message) {
        if (message.getImage() != null) return message.getImage();
        if (message.getDocument() != null) return message.getDocument();
        if (message.getAudio() != null) return message.getAudio();
        if (message.getVideo() != null) return message.getVideo();
        return null;
    }
}
