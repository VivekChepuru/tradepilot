package com.tradepilot.core.trade.negotiation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NegotiationSettingsRepository extends JpaRepository<NegotiationSettings, Long> {
    Optional<NegotiationSettings> findTopByOrderByUpdatedAtDesc();
}
