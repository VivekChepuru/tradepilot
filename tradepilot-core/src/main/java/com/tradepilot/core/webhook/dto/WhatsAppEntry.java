package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppEntry {

    @JsonProperty("id")
    private String id;

    @JsonProperty("changes")
    private List<WhatsAppChange> changes;
}