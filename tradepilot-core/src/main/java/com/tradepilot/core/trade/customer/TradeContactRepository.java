package com.tradepilot.core.trade.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeContactRepository extends JpaRepository<TradeContact, Long> {
    Optional<TradeContact> findByWhatsappNumber(String number);
}
