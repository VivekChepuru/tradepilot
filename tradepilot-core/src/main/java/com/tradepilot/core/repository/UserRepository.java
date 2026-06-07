package com.tradepilot.core.repository;

import com.tradepilot.core.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserId(UUID userId);

    // Lookup by phone number (for login / contact discovery)
    Optional<User> findByPhoneNumber(String phoneNumber);

    // Check uniqueness during registration
    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByUsername(String username);

    // 🔹 NEW: bulk fetch users by external userId
    List<User> findByUserIdIn(List<UUID> userIds);
}
