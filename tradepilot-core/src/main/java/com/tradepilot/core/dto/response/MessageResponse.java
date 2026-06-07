package com.tradepilot.core.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MessageResponse {
    private UUID id;             // Message ID
    private UUID chatId;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Sender info
    private UUID senderId;
    private String senderUsername;
    private String senderPhoneNumber;  // Optional: might be useful

    // Optional: for future features
    private UUID replyToMessageId;
    private boolean isEdited;
    private boolean isDeleted;
}
