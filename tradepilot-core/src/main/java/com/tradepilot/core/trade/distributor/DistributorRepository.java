package com.tradepilot.core.trade.distributor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    List<Distributor> findByIsActiveTrue();
    Optional<Distributor> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
