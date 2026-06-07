package com.tradepilot.core.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypingIndicatorDTO {
    private UUID chatId;
    private UUID userId;
    private String username;
    private boolean typing;
}
