package com.tradepilot.core.trade.pricing;

import com.tradepilot.core.exception.PriceRuleNotFoundException;
import com.tradepilot.core.trade.distributor.Distributor;
import com.tradepilot.core.trade.distributor.DistributorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int INTERMEDIATE_SCALE = 10;

    private final PriceRuleRepository priceRuleRepository;
    private final DistributorRepository distributorRepository;

    public PriceQuote calculateQuote(String commodity, String grade, Double quantity, String unit) {
        return calculateQuote(commodity, grade, quantity, unit, null);
    }

    public PriceQuote calculateQuote(String commodity, String grade, Double quantity, String unit, String distributorName) {
        PriceRule rule = resolvePriceRule(commodity, grade, distributorName);

        BigDecimal effectivePrice = rule.getBasePrice()
                .add(rule.getFreightPerUnit())
                .multiply(BigDecimal.ONE.add(
                        rule.getMarginPercent().divide(HUNDRED, INTERMEDIATE_SCALE, RoundingMode.HALF_UP)));

        BigDecimal gstAmount = effectivePrice
                .multiply(rule.getGstPercent().divide(HUNDRED, INTERMEDIATE_SCALE, RoundingMode.HALF_UP));

        BigDecimal finalPricePerUnit = effectivePrice.add(gstAmount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAmount = quantity != null
                ? finalPricePerUnit.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP)
                : null;

        log.debug("Calculated quote for commodity={}, grade={}: finalPrice={}", commodity, grade, finalPricePerUnit);

        return new PriceQuote(
                commodity,
                grade,
                rule.getBasePrice(),
                rule.getMarginPercent(),
                rule.getGstPercent(),
                rule.getFreightPerUnit(),
                effectivePrice.setScale(2, RoundingMode.HALF_UP),
                gstAmount.setScale(2, RoundingMode.HALF_UP),
                finalPricePerUnit,
                totalAmount,
                unit != null ? unit : rule.getUnit(),
                LocalDateTime.now()
        );
    }

    private PriceRule resolvePriceRule(String commodity, String grade, String distributorName) {
        if (distributorName != null) {
            Optional<Distributor> distributor = distributorRepository.findByNameIgnoreCase(distributorName);
            if (distributor.isPresent()) {
                Optional<PriceRule> tier1 = priceRuleRepository
                        .findFirstByCommodityIgnoreCaseAndGradeIgnoreCaseAndDistributorIdAndIsActiveTrue(
                                commodity, grade, distributor.get().getId());
                if (tier1.isPresent()) {
                    log.info("Price rule resolved — tier=1 commodity={} grade={} distributor={} for messageId=unknown",
                            commodity, grade, distributorName);
                    return tier1.get();
                }
            }
        }

        Optional<PriceRule> tier2 = priceRuleRepository
                .findFirstByCommodityIgnoreCaseAndGradeIgnoreCaseAndDistributorIdIsNullAndIsActiveTrue(commodity, grade);
        if (tier2.isPresent()) {
            log.info("Price rule resolved — tier=2 commodity={} grade={} (no distributor match) for messageId=unknown",
                    commodity, grade);
            return tier2.get();
        }

        Optional<PriceRule> tier3 = priceRuleRepository
                .findFirstByCommodityIgnoreCaseAndGradeIsNullAndDistributorIdIsNullAndIsActiveTrue(commodity);
        if (tier3.isPresent()) {
            log.info("Price rule resolved — tier=3 commodity={} (grade+distributor fallback) for messageId=unknown",
                    commodity);
            return tier3.get();
        }

        throw new PriceRuleNotFoundException(
                String.format("No price rule found for commodity=%s grade=%s distributor=%s", commodity, grade, distributorName));
    }
}
