package com.tradepilot.core.trade.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/payments/overdue")
@RequiredArgsConstructor
public class OverduePaymentController {

    private final OverduePaymentService overduePaymentService;

    private static final Set<String> VALID_TONES = Set.of("POLITE", "FIRM", "LEGAL_WARNING");

    @GetMapping
    public ResponseEntity<List<PaymentOverdueFlag>> getOpenFlags() {
        return ResponseEntity.ok(overduePaymentService.getOpenFlags());
    }

    @PostMapping("/{flagId}/remind")
    public ResponseEntity<Object> sendReminder(@PathVariable Long flagId, @RequestBody ReminderRequest request) {
        String tone = request.tone();
        if (!VALID_TONES.contains(tone)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid tone. Must be POLITE, FIRM, or LEGAL_WARNING"));
        }
        try {
            overduePaymentService.sendManualReminder(flagId, tone);
            return ResponseEntity.ok(Map.of("message", "Reminder sent", "flagId", flagId, "tone", tone));
        } catch (IllegalArgumentException e) {
            log.warn("Flag not found: flagId={}", flagId);
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to send reminder for flagId={}", flagId, e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send reminder"));
        }
    }

    @PostMapping("/{flagId}/paid")
    public ResponseEntity<Object> markAsPaid(@PathVariable Long flagId) {
        try {
            overduePaymentService.markAsPaid(flagId);
            return ResponseEntity.ok(Map.of("message", "Order marked as paid", "flagId", flagId));
        } catch (IllegalArgumentException e) {
            log.warn("Flag not found: flagId={}", flagId);
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/run-check")
    public ResponseEntity<Map<String, String>> runCheck() {
        try {
            overduePaymentService.flagOverdueOrders();
            return ResponseEntity.ok(Map.of("message", "Overdue check completed"));
        } catch (Exception e) {
            log.error("Manual overdue check failed", e);
            return ResponseEntity.status(500).body(Map.of("error", "Overdue check failed"));
        }
    }

    record ReminderRequest(String tone) {}
}
