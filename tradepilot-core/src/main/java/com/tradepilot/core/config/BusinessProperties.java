package com.tradepilot.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties(prefix = "tradepilot.business")
public class BusinessProperties {

    private Bank bank;
    private Invoice invoice;

    @Data
    public static class Bank {
        private String accountName;
        private String accountNumber;
        private String ifscCode;
        private String bankName;
        private String upiId;
    }

    @Data
    public static class Invoice {
        private BigDecimal gstRate;
        private int dueDaysPostDelivery;
        private int dueDaysAdvance;
    }
}
