package com.tradepilot.core.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "tradepilot.whatsapp.simulation-mode", havingValue = "false")
public class RealWhatsAppSenderService implements WhatsAppSenderService {

    @Override
    public SendResult send(String toNumber, String message) {
        log.warn("Real WhatsApp API not yet configured");
        return new SendResult(false, null, "Real WhatsApp API not yet configured");
    }
}