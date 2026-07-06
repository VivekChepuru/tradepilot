package com.tradepilot.core.trade.negotiation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NegotiationOverrideRepository extends JpaRepository<NegotiationOverride, Long> {
    Optional<NegotiationOverride> findByCommodityIgnoreCase(String commodity);
    List<NegotiationOverride> findAllByOrderByCommodityAsc();
    boolean existsByCommodityIgnoreCase(String commodity);
}
