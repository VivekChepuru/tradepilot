package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMediaBody {

    @JsonProperty("id")
    private String id;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("sha256")
    private String sha256;

    @JsonProperty("caption")
    private String caption;
}