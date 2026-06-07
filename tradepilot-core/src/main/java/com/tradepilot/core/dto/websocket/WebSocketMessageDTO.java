package com.tradepilot.core.dto.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessageDTO {
    private UUID id;
    private UUID chatId;
    private UUID senderId;
    private String senderUsername;
    private String content;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private MessageStatus status;

    public enum MessageStatus {
        SENT, DELIVERED, READ
    }
}
