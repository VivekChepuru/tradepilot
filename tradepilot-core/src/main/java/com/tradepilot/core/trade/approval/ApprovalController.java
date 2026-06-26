package com.tradepilot.core.trade.approval;

import com.tradepilot.core.channel.WhatsAppSenderService;
import com.tradepilot.core.trade.negotiation.NegotiationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final PendingApprovalStore pendingApprovalStore;
    private final NegotiationService negotiationService;
    private final WhatsAppSenderService whatsAppSenderService;

    @GetMapping
    public ResponseEntity<List<PendingApproval>> getOpenApprovals() {
        List<PendingApproval> approvals = pendingApprovalStore.getAll();
        log.info("Fetching pending approvals — count={}", approvals.size());
        return ResponseEntity.ok(approvals);
    }

    @PostMapping("/{messageId}/approve")
    public ResponseEntity<Map<String, String>> approve(
            @PathVariable String messageId,
            @RequestBody ApproveRequest request) {
        PendingApproval approval = pendingApprovalStore.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Approval not found: " + messageId));
        try {
            whatsAppSenderService.send(approval.fromNumber(),
                    "Your negotiation has been approved. Discount of " + request.discountPercent() + "% applied.");
            pendingApprovalStore.remove(messageId);
            return ResponseEntity.ok(Map.of("message", "Approved and sent"));
        } catch (Exception e) {
            log.error("Failed to approve messageId={}", messageId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process approval");
        }
    }

    @PostMapping("/{messageId}/reject")
    public ResponseEntity<Map<String, String>> reject(@PathVariable String messageId) {
        PendingApproval approval = pendingApprovalStore.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Approval not found: " + messageId));
        try {
            whatsAppSenderService.send(approval.fromNumber(),
                    "We appreciate your interest but cannot accommodate the requested discount at this time.");
            pendingApprovalStore.remove(messageId);
            return ResponseEntity.ok(Map.of("message", "Rejected and notified"));
        } catch (Exception e) {
            log.error("Failed to reject messageId={}", messageId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process rejection");
        }
    }

    record ApproveRequest(double discountPercent) {}
}
