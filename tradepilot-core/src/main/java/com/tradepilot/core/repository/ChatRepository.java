package com.tradepilot.core.repository;

import com.tradepilot.core.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    // Fetch multiple chats by their UUIDs (useful for group chats)
    List<Chat> findAllByIdIn(List<UUID> chatIds);

    // Optional: search by name (case-insensitive)
    List<Chat> findByChatNameIgnoreCase(String chatName);
}
