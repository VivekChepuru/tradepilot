package com.tradepilot.core.service;

import com.tradepilot.core.entity.Chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatService {

    Chat createChat(String chatName, boolean isGroup, List<UUID> participantIds);

    Optional<Chat> getChat(UUID chatId);

    List<Chat> getAllChats();

    List<Chat> getChatsByIds(List<UUID> chatIds);

    void deleteChat(UUID chatId);

    Chat addUserToChat(UUID chatId, UUID userId);

    Chat removeUserFromChat(UUID chatId, UUID userId);
}
