package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppContact {

    @JsonProperty("profile")
    private WhatsAppProfile profile;

    @JsonProperty("wa_id")
    private String waId;
}