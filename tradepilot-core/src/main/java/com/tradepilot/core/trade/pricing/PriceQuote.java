package com.tradepilot.core.trade.pricing;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PriceQuote(
        String commodity,
        String grade,
        BigDecimal basePrice,
        BigDecimal marginPercent,
        BigDecimal gstPercent,
        BigDecimal freightPerUnit,
        BigDecimal effectivePrice,
        BigDecimal gstAmount,
        BigDecimal finalPricePerUnit,
        BigDecimal totalAmount,
        String unit,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime calculatedAt
) {}