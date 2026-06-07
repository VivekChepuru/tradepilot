package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppChange {

    @JsonProperty("value")
    private WhatsAppValue value;

    @JsonProperty("field")
    private String field;
}