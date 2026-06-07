package com.tradepilot.core.trade.pricing;

import com.tradepilot.core.exception.PriceRuleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceCalculationService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int INTERMEDIATE_SCALE = 10;

    private final PriceRuleRepository priceRuleRepository;

    public PriceQuote calculateQuote(String commodity, String grade, Double quantity, String unit) {
        PriceRule rule = priceRuleRepository.findBestMatch(commodity, grade)
                .orElseThrow(() -> new PriceRuleNotFoundException(
                        "No price rule found for commodity: " + commodity + ", grade: " + grade));

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
}