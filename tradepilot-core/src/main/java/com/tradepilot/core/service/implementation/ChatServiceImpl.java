package com.tradepilot.core.service.implementation;

import com.tradepilot.core.entity.Chat;
import com.tradepilot.core.entity.User;
import com.tradepilot.core.repository.ChatRepository;
import com.tradepilot.core.repository.UserRepository;
import com.tradepilot.core.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Chat createChat(String chatName, boolean isGroup, List<UUID> participantUserIds) {
        try {
            System.out.println("=== CREATE CHAT DEBUG ===");
            System.out.println("chatName: " + chatName);
            System.out.println("isGroup: " + isGroup);
            System.out.println("participantIds: " + participantUserIds);

            if (isGroup && (chatName == null || chatName.trim().isEmpty())) {
                throw new IllegalArgumentException("Group chat must have a name.");
            }

            if (participantUserIds == null || participantUserIds.isEmpty()) {
                throw new IllegalArgumentException("Chat must have at least one participant.");
            }

            System.out.println("Fetching users...");
            List<User> users = userRepository.findAllById(participantUserIds);
            System.out.println("Found " + users.size() + " users");

            if (users.size() != participantUserIds.size()) {
                Set<UUID> foundIds = users.stream()
                        .map(User::getId)
                        .collect(Collectors.toSet());

                Set<UUID> missing = participantUserIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .collect(Collectors.toSet());

                throw new IllegalArgumentException("The following participant IDs were not found: " + missing);
            }

            System.out.println("Creating chat object...");
            Chat chat = new Chat();
            chat.setChatName(chatName);
            chat.setGroup(isGroup);
            chat.setParticipants(new HashSet<>(users));

            System.out.println("Saving chat...");
            Chat savedChat = chatRepository.save(chat);
            System.out.println("Chat saved successfully with ID: " + savedChat.getId());

            return savedChat;

        } catch (Exception e) {
            System.err.println("ERROR in createChat: " + e.getClass().getName());
            System.err.println("ERROR message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public Optional<Chat> getChat(UUID chatId) {
        return chatRepository.findById(chatId);
    }

    @Override
    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    @Override
    public List<Chat> getChatsByIds(List<UUID> chatIds) {
        return chatRepository.findAllByIdIn(chatIds);
    }

    @Override
    public void deleteChat(UUID chatId) {
        Chat chat = chatRepository.findById(chatId)
                        .orElseThrow(()-> new IllegalArgumentException("Chat with ID " + chatId + " not found."));
        chatRepository.delete(chat);
    }

    @Override
    public Chat addUserToChat(UUID chatId, UUID userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // ✅ add() returns false if user was ALREADY in the set
        // ✅ add() returns true if user was SUCCESSFULLY added
        if (!chat.getParticipants().add(user)) {
            throw new IllegalArgumentException("User already in chat.");
        }

        return chatRepository.save(chat);
    }

    @Override
    public Chat removeUserFromChat(UUID chatId, UUID userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(()-> new IllegalArgumentException("Chat not found."));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(()-> new IllegalArgumentException("User not found."));

        if (!chat.getParticipants().remove(user)) {
            throw new IllegalArgumentException("User is not part of this chat.");
        }

        return chatRepository.save(chat);
    }
}
