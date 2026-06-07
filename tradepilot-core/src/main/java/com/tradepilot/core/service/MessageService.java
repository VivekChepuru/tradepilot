package com.tradepilot.core.service;

import com.tradepilot.core.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageService {

    // Updated: removed senderUserId parameter
    Message sendMessage(UUID authenticatedUserId, UUID chatId, String content);

    Optional<Message> getMessage(UUID messageId);

    List<Message> getMessagesByChat(UUID chatId);

    void deleteMessage(UUID messageId, UUID authenticatedUserId);  // Only sender can delete

    // Optional: for future features
    Message editMessage(UUID messageId, UUID authenticatedUserId, String newContent);
}
