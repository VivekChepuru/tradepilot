package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiResultEvent {

    @JsonProperty("whatsappMessageId")
    private String messageId;

    @JsonProperty("fromNumber")
    private String from;
    private String senderName;
    private String phoneNumberId;
    private String displayPhoneNumber;
    private String timestamp;
    private String messageType;
    private String textBody;

    private String detectedIntent;
    @JsonProperty("confidenceScore")
    private Double confidence;
    private Map<String, Object> extractedEntities;
}