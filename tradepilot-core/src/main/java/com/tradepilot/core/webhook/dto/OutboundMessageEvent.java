package com.tradepilot.core.webhook.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboundMessageEvent {

    private String whatsappMessageId;
    private String fromNumber;
    private String suggestedReply;
    private String routingDecision;
    private String commodity;
    private String grade;
    private BigDecimal finalPricePerUnit;
    private BigDecimal totalAmount;
    private String unit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processedAt;
}