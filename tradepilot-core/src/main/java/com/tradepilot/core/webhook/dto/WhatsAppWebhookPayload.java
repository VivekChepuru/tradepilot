package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookPayload {

    @JsonProperty("object")
    private String object;

    @JsonProperty("entry")
    private List<WhatsAppEntry> entry;
}