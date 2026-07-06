package com.tradepilot.core.trade.pricing;

import com.tradepilot.core.trade.distributor.DistributorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/price-rules")
@RequiredArgsConstructor
public class PriceRuleController {

    private final PriceRuleRepository priceRuleRepository;
    private final DistributorRepository distributorRepository;

    @GetMapping
    public ResponseEntity<List<PriceRule>> getAllPriceRules() {
        List<PriceRule> rules = priceRuleRepository.findAll(Sort.by(Sort.Direction.ASC, "commodity"));
        log.info("Fetched {} price rules", rules.size());
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceRule> getPriceRuleById(@PathVariable Long id) {
        PriceRule rule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price rule not found: " + id));
        return ResponseEntity.ok(rule);
    }

    @PostMapping
    public ResponseEntity<PriceRule> createPriceRule(@RequestBody PriceRule priceRule) {
        if (priceRule.getCommodity() == null || priceRule.getCommodity().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "commodity must not be blank");
        }

        priceRule.setId(null);
        PriceRule saved = priceRuleRepository.save(priceRule);
        log.info("Price rule created: commodity={} grade={}", saved.getCommodity(), saved.getGrade());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PriceRule> updatePriceRule(@PathVariable Long id, @RequestBody PriceRule updates) {
        PriceRule existing = priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price rule not found: " + id));

        existing.setBasePrice(updates.getBasePrice());
        existing.setMarginPercent(updates.getMarginPercent());
        existing.setFreightPerUnit(updates.getFreightPerUnit());
        existing.setGstPercent(updates.getGstPercent());
        existing.setUnit(updates.getUnit());
        existing.setDistributorId(updates.getDistributorId());
        existing.setDistributorName(updates.getDistributorName());
        existing.setActive(updates.isActive());

        PriceRule saved = priceRuleRepository.save(existing);
        log.info("Price rule updated: id={} commodity={}", saved.getId(), saved.getCommodity());
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<PriceRule> toggleActive(@PathVariable Long id) {
        PriceRule rule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price rule not found: " + id));

        rule.setActive(!rule.isActive());
        PriceRule saved = priceRuleRepository.save(rule);
        log.info("Price rule id={} toggled to isActive={}", saved.getId(), saved.isActive());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePriceRule(@PathVariable Long id) {
        PriceRule rule = priceRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price rule not found: " + id));

        priceRuleRepository.delete(rule);
        log.info("Price rule deleted: id={}", id);
        return ResponseEntity.noContent().build();
    }
}
