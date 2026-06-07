package com.tradepilot.core.channel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Primary
@ConditionalOnProperty(name = "tradepilot.whatsapp.simulation-mode", havingValue = "true")
public class SimulatedWhatsAppSenderService implements WhatsAppSenderService {

    @Override
    public SendResult send(String toNumber, String message) {
        log.info("SIMULATED SEND → to: {} message: {}", toNumber, message);
        return new SendResult(true, "sim-" + UUID.randomUUID(), null);
    }
}