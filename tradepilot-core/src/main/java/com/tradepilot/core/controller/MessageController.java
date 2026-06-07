package com.tradepilot.core.controller;

import com.tradepilot.core.dto.request.MessageCreateRequest;
import com.tradepilot.core.dto.response.MessageResponse;
import com.tradepilot.core.dto.websocket.WebSocketMessageDTO;
import com.tradepilot.core.entity.Message;
import com.tradepilot.core.entity.User;
import com.tradepilot.core.mapper.MessageMapper;
import com.tradepilot.core.repository.UserRepository;
import com.tradepilot.core.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Send a message
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageCreateRequest request,
            Authentication authentication) {

        System.out.println("=== SENDING MESSAGE ===");
        System.out.println("Chat ID: " + request.getChatId());
        System.out.println("Content: " + request.getContent());

        // Get authenticated user
        String phoneNumber = authentication.getName();
        User sender = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        // Send message
        Message message = messageService.sendMessage(
                sender.getId(),
                request.getChatId(),
                request.getContent()
        );

        MessageResponse response = MessageMapper.toResponse(message);

        // BROADCAST MESSAGE VIA WEBSOCKET
        WebSocketMessageDTO wsMessage  = new WebSocketMessageDTO(
                message.getId(),
                message.getChat().getId(),
                sender.getId(),
                sender.getUsername(),
                message.getContent(),
                message.getTimestamp(),
                WebSocketMessageDTO.MessageStatus.SENT
        );

        System.out.println("=== BROADCASTING MESSAGE ===");
        System.out.println("Topic: /topic/chat/" + request.getChatId());
        System.out.println("Message: " + wsMessage);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + request.getChatId(),
                wsMessage
        );

        System.out.println("=== BROADCAST COMPLETE ===");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all messages in a chat
    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<MessageResponse>> getMessagesByChat(@PathVariable UUID chatId) {
        List<MessageResponse> message = messageService.getMessagesByChat(chatId)
                .stream()
                .map(MessageMapper::toResponse)
                .toList();

        return ResponseEntity.ok(message);
    }

   // Get a single message by ID
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable UUID messageId) {
        Message message = messageService.getMessage(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        return ResponseEntity.ok(MessageMapper.toResponse(message));
    }

    // Delete a message (only sender can delete)
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            Authentication authentication) {

        // Get authenticated user
        String phoneNumber = authentication.getName();
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        messageService.deleteMessage(messageId, user.getId());

        return ResponseEntity.noContent().build();
    }

    // Edit a message (only sender can edit)
    @PutMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(
            @PathVariable UUID messageId,
            @RequestParam String newContent,
            Authentication authentication) {

        // Get authenticated user
        String phoneNumber = authentication.getName();
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        Message message = messageService.editMessage(messageId, user.getId(), newContent);

        return ResponseEntity.ok(MessageMapper.toResponse(message));
    }
}
