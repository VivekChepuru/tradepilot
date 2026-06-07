package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMessage {

    @JsonProperty("id")
    private String id;

    @JsonProperty("from")
    private String from;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("type")
    private String type;

    @JsonProperty("text")
    private WhatsAppTextBody text;

    @JsonProperty("image")
    private WhatsAppMediaBody image;

    @JsonProperty("document")
    private WhatsAppMediaBody document;

    @JsonProperty("audio")
    private WhatsAppMediaBody audio;

    @JsonProperty("video")
    private WhatsAppMediaBody video;
}