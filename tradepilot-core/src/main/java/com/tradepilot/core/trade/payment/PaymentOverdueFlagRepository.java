package com.tradepilot.core.trade.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentOverdueFlagRepository extends JpaRepository<PaymentOverdueFlag, Long> {
    List<PaymentOverdueFlag> findByStatus(String status);
    Optional<PaymentOverdueFlag> findByOrderIdAndStatus(Long orderId, String status);
    boolean existsByOrderIdAndStatus(Long orderId, String status);
    List<PaymentOverdueFlag> findByStatusOrderByFlaggedAtDesc(String status);
}
