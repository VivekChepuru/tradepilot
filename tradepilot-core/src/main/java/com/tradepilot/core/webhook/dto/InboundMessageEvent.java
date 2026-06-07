package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundMessageEvent {

    private String messageId;
    private String from;
    private String senderName;
    private String phoneNumberId;
    private String displayPhoneNumber;
    private String timestamp;
    private String messageType;
    private String textBody;
    private String mediaId;
    private String mediaCaption;
}
