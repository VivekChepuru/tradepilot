package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppValue {

    @JsonProperty("messaging_product")
    private String messagingProduct;

    @JsonProperty("metadata")
    private WhatsAppMetadata metadata;

    @JsonProperty("contacts")
    private List<WhatsAppContact> contacts;

    @JsonProperty("messages")
    private List<WhatsAppMessage> messages;
}