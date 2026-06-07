package com.tradepilot.core.webhook;

import com.tradepilot.core.webhook.dto.WhatsAppWebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WhatsAppWebhookService webhookService;

    @Value("${tradepilot.whatsapp.verify-token}")
    private String verifyToken;

    /**
     * Meta webhook verification handshake.
     * Meta sends: GET /webhook?hub.mode=subscribe&hub.verify_token=<token>&hub.challenge=<challenge>
     */
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("WhatsApp webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }

        log.warn("WhatsApp webhook verification failed: mode={}, token matches={}", mode, verifyToken.equals(token));
        return ResponseEntity.status(403).build();
    }

    /**
     * Receives inbound WhatsApp messages from Meta.
     */
    @PostMapping
    public ResponseEntity<Void> receiveMessage(@RequestBody WhatsAppWebhookPayload payload) {
        log.debug("Received WhatsApp webhook payload: object={}", payload.getObject());
        webhookService.processPayload(payload);
        return ResponseEntity.ok().build();
    }
}
