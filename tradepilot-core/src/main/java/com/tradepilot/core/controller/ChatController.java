package com.tradepilot.core.controller;

import com.tradepilot.core.dto.request.ChatCreateRequest;
import com.tradepilot.core.dto.response.ChatResponse;
import com.tradepilot.core.entity.Chat;
import com.tradepilot.core.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private ChatService chatService;
    //CREATE ONE-ON-ONE/GROUP CHAT
    @PostMapping
    public ResponseEntity<ChatResponse> createChat(@Valid @RequestBody ChatCreateRequest req) {
        Chat chat = chatService.createChat(req.getChatName(), req.getIsGroup(), req.getParticipantIds());

        ChatResponse resp = toResponse(chat);
        return ResponseEntity.status(201).body(resp);
    }

    private ChatResponse toResponse(Chat chat) {
        ChatResponse r = new ChatResponse();
        r.setId(chat.getId());
        r.setChatName(chat.getChatName());
        r.setGroup(chat.isGroup());
        r.setParticipants(chat.getParticipants().stream().map(u -> {
            ChatResponse.ParticipantDto p = new ChatResponse.ParticipantDto();
            p.setUserId(u.getUserId());
            p.setUsername(u.getUsername());
            p.setPhoneNumber(u.getPhoneNumber());
            return p;
        }).collect(Collectors.toList()));
        return r;
    }
    //Helper method -safe UUID parser
    public static UUID safeToUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null; // or throw a custom exception
        }
    }
    //GET CHAT BY CHAT_ID
    //TODO: Need to test
    @GetMapping("/{chatId}")
    public ResponseEntity<ChatResponse> getChatById(@PathVariable UUID chatId) {
        Chat chat = chatService.getChat(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with ID: " + chatId));
        return ResponseEntity.ok(toResponse(chat));
    }
    //GET ALL CHATS
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getAllChats() {
        List<ChatResponse> chats = chatService.getAllChats()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(chats);
    }

    //ADD USER TO CHAT
    @PostMapping("/{chatId}/add-user/{userId}")
    public ResponseEntity<ChatResponse> addUserToChat(@PathVariable UUID chatId, @PathVariable UUID userId) {
        Chat chat = chatService.addUserToChat(chatId, userId);
        return ResponseEntity.ok(toResponse(chat));
    }

    //REMOVE USER FROM CHAT
    @PostMapping("/{chatId}/remove-user/{userId}")
    public ResponseEntity<ChatResponse> removeUserFromChat(@PathVariable UUID chatId, @PathVariable UUID userId) {
        Chat chat = chatService.removeUserFromChat(chatId, userId);
        return ResponseEntity.ok(toResponse(chat));
    }

    //DELETE CHATS BY CHAT_ID
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable UUID chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }
}
