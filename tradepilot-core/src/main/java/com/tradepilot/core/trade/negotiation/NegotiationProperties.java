package com.tradepilot.core.trade.negotiation;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tradepilot.negotiation")
public class NegotiationProperties {

    private double maxAutoDiscountPercent;
    private double maxEscalateDiscountPercent;
}