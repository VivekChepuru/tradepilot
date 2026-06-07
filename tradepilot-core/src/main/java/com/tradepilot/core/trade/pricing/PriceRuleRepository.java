package com.tradepilot.core.trade.pricing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    Optional<PriceRule> findFirstByCommodityAndGradeAndIsActiveTrue(String commodity, String grade);

    Optional<PriceRule> findFirstByCommodityAndGradeIsNullAndIsActiveTrue(String commodity);

    default Optional<PriceRule> findBestMatch(String commodity, String grade) {
        return findFirstByCommodityAndGradeAndIsActiveTrue(commodity, grade)
                .or(() -> findFirstByCommodityAndGradeIsNullAndIsActiveTrue(commodity));
    }
}