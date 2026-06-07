package com.tradepilot.core.controller;

import com.tradepilot.core.dto.websocket.TypingIndicatorDTO;
import com.tradepilot.core.dto.websocket.UserStatusDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle typing indicator
     * Client sends to: /app/typing
     * Server broadcasts to: /topic/chat/{chatId}/typing
     */
    @MessageMapping("/typing")
    public void handleTypingIndicator(@Payload TypingIndicatorDTO typingIndicator) {
        // Broadcast typing status to all users in the chat
//        messagingTemplate.convertAndSend(
//                "/topic/chat/" + typingIndicator.getChatId() + "/typing",
//                typingIndicator
//        );

        System.out.println("=== TYPING INDICATOR RECEIVED ===");
        System.out.println("Chat ID: " + typingIndicator.getChatId());
        System.out.println("Username: " + typingIndicator.getUsername());
        System.out.println("Is Typing: " + typingIndicator.isTyping());

        String topic = "/topic/chat/" + typingIndicator.getChatId() + "/typing";
        System.out.println("Broadcasting to: " + topic);

        messagingTemplate.convertAndSend(topic, typingIndicator);

        System.out.println("=== TYPING INDICATOR SENT ===");
    }

    /**
     * Handle user status updates (online/offline)
     * Client sends to: /app/status
     * Server broadcasts to: /topic/status
     */
    @MessageMapping("/status")
    public void handleUserStatus(@Payload UserStatusDTO userStatus) {
        // Broadcast user status to all connected clients
        messagingTemplate.convertAndSend(
                "/topic/status",
                userStatus
        );
    }

    /**
     * Send message to specific chat
     */
    public void sendMessageToChat(UUID chatId, Object message) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + chatId,
                message
        );
    }

    /**
     * Send private message to specific user
     */
    public void sendMessageToUser(UUID userId, Object message) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/messages",
                message
        );
    }
}
