package com.tradepilot.core.trade.negotiation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/negotiation-settings")
@RequiredArgsConstructor
public class NegotiationSettingsController {

    private final NegotiationSettingsRepository negotiationSettingsRepository;
    private final NegotiationOverrideRepository negotiationOverrideRepository;

    @GetMapping
    public ResponseEntity<NegotiationSettings> getGlobalSettings() {
        NegotiationSettings settings = negotiationSettingsRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No negotiation settings found"));
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<NegotiationSettings> updateGlobalSettings(@RequestBody NegotiationSettings updates) {
        NegotiationSettings existing = negotiationSettingsRepository.findTopByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No negotiation settings found"));

        existing.setMaxAutoDiscountPercent(updates.getMaxAutoDiscountPercent());
        existing.setMaxEscalateDiscountPercent(updates.getMaxEscalateDiscountPercent());
        existing.setIsNegotiationEnabled(updates.getIsNegotiationEnabled());
        existing.setUpdatedBy(updates.getUpdatedBy());

        NegotiationSettings saved = negotiationSettingsRepository.save(existing);
        log.info("Global negotiation settings updated: auto={}% escalate={}% enabled={}",
                saved.getMaxAutoDiscountPercent(), saved.getMaxEscalateDiscountPercent(), saved.getIsNegotiationEnabled());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/overrides")
    public ResponseEntity<List<NegotiationOverride>> getOverrides() {
        return ResponseEntity.ok(negotiationOverrideRepository.findAllByOrderByCommodityAsc());
    }

    @PostMapping("/overrides")
    public ResponseEntity<NegotiationOverride> createOverride(@RequestBody NegotiationOverride override) {
        if (override.getCommodity() == null || override.getCommodity().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commodity must not be blank");
        }
        if (negotiationOverrideRepository.existsByCommodityIgnoreCase(override.getCommodity())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Negotiation override already exists for commodity: " + override.getCommodity());
        }

        override.setId(null);
        NegotiationOverride saved = negotiationOverrideRepository.save(override);
        log.info("Negotiation override created for commodity={}", saved.getCommodity());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/overrides/{id}")
    public ResponseEntity<NegotiationOverride> updateOverride(@PathVariable Long id, @RequestBody NegotiationOverride updates) {
        NegotiationOverride existing = negotiationOverrideRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negotiation override not found: " + id));

        existing.setMaxAutoDiscountPercent(updates.getMaxAutoDiscountPercent());
        existing.setMaxEscalateDiscountPercent(updates.getMaxEscalateDiscountPercent());
        existing.setIsNegotiationEnabled(updates.getIsNegotiationEnabled());
        existing.setUpdatedBy(updates.getUpdatedBy());

        NegotiationOverride saved = negotiationOverrideRepository.save(existing);
        log.info("Negotiation override updated: id={} commodity={}", saved.getId(), saved.getCommodity());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/overrides/{id}")
    public ResponseEntity<Void> deleteOverride(@PathVariable Long id) {
        NegotiationOverride existing = negotiationOverrideRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Negotiation override not found: " + id));

        negotiationOverrideRepository.delete(existing);
        log.info("Negotiation override deleted for id={}", id);
        return ResponseEntity.noContent().build();
    }
}
