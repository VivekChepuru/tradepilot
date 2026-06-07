package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppMetadata {

    @JsonProperty("display_phone_number")
    private String displayPhoneNumber;

    @JsonProperty("phone_number_id")
    private String phoneNumberId;
}