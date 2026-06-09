package com.tradepilot.core.workflow;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FollowUpMessageBuilder {

    private static final Map<String, String> TEMPLATES = Map.of(
            "INQUIRY_FOLLOWUP_1",
            "Hi {name}, just checking if you received our price for {commodity} {grade} at ₹{price}/MT. Happy to answer any questions!",

            "INQUIRY_FOLLOWUP_2",
            "Following up on {commodity} {grade} quote. Price valid today. Shall we proceed?",

            "INQUIRY_FOLLOWUP_3",
            "Last follow-up on your {commodity} inquiry. Let us know if you need a revised quote.",

            "PAYMENT_REMINDER_DUE",
            "Hi {name}, your payment for order {ref} is due today. Please arrange at your earliest convenience.",

            "PAYMENT_REMINDER_3D",
            "Gentle reminder — payment for order {ref} was due 3 days ago. Please update us on the status.",

            "PAYMENT_REMINDER_7D",
            "Important: payment for order {ref} is now 7 days overdue. Please contact us to resolve this.",

            "PAYMENT_REMINDER_15D",
            "URGENT: payment for order {ref} is 15 days overdue. Please contact us immediately to avoid further action."
    );

    public String buildMessage(String template, Map<String, String> context) {
        String message = TEMPLATES.getOrDefault(template, template);
        for (Map.Entry<String, String> entry : context.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }
}
