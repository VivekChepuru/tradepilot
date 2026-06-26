package com.tradepilot.core.trade.approval;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PendingApproval(
    String whatsappMessageId,
    String fromNumber,
    String commodity,
    String grade,
    BigDecimal requestedDiscountPercent,
    BigDecimal originalPrice,
    BigDecimal discountedPrice,
    String routingDecision,
    LocalDateTime processedAt
) {}
