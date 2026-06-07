package com.tradepilot.core.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class MessageCreateRequest {
    @NotNull(message = "Chat ID is required")
    private UUID chatId;

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 5000, message = "Message cannot exceed 5000 characters")
    private String content;

    // Optional: for future features
    private UUID replyToMessageId;  // For threading/replies

    // Note: No senderId - we get this from authenticated user!
}
