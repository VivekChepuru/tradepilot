package com.tradepilot.core.repository;

import com.tradepilot.core.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Get all messages in a chat (sorted by timestamp)
    List<Message> findByChat_IdOrderByTimestampAsc(UUID chatId);

    // Find messages by sender
    List<Message> findBySender_IdOrderByTimestampAsc(UUID senderId);

}
