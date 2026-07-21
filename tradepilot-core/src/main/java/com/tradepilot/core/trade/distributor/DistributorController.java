package com.tradepilot.core.trade.distributor;

import com.tradepilot.core.trade.pricing.PriceRuleRepository;
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
@RequestMapping("/api/distributors")
@RequiredArgsConstructor
public class DistributorController {

    private final DistributorRepository distributorRepository;
    private final PriceRuleRepository priceRuleRepository;

    @GetMapping
    public ResponseEntity<List<Distributor>> getAllDistributors() {
        List<Distributor> distributors = distributorRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        log.info("Fetching all distributors — count={}", distributors.size());
        return ResponseEntity.ok(distributors);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Distributor>> getActiveDistributors() {
        List<Distributor> distributors = distributorRepository.findByIsActiveTrue();
        log.info("Fetching active distributors — count={}", distributors.size());
        return ResponseEntity.ok(distributors);
    }

    @PostMapping
    public ResponseEntity<Distributor> createDistributor(@RequestBody Distributor distributor) {
        if (distributor.getName() == null || distributor.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
        }

        if (distributorRepository.existsByNameIgnoreCase(distributor.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Distributor with name already exists");
        }

        distributor.setId(null);
        Distributor saved = distributorRepository.save(distributor);
        log.info("Distributor created: name={}", saved.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Distributor> updateDistributor(@PathVariable Long id, @RequestBody Distributor updates) {
        Distributor existing = distributorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Distributor not found: " + id));

        existing.setContactName(updates.getContactName());
        existing.setPhone(updates.getPhone());
        existing.setCity(updates.getCity());
        existing.setIsActive(updates.getIsActive());

        Distributor saved = distributorRepository.save(existing);
        log.info("Distributor updated: id={} name={}", saved.getId(), saved.getName());
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Distributor> toggleActive(@PathVariable Long id) {
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Distributor not found: " + id));

        distributor.setIsActive(!Boolean.TRUE.equals(distributor.getIsActive()));
        Distributor saved = distributorRepository.save(distributor);
        log.info("Distributor id={} toggled to isActive={}", saved.getId(), saved.getIsActive());
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDistributor(@PathVariable Long id) {
        Distributor distributor = distributorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Distributor not found: " + id));

        if (priceRuleRepository.existsByDistributorId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete distributor with existing price rules");
        }

        distributorRepository.delete(distributor);
        log.info("Distributor deleted: id={} name={}", id, distributor.getName());
        return ResponseEntity.noContent().build();
    }
}