package com.tradepilot.core.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDTO {
    private UUID userId;
    private String username;
    private UserStatus status;

    public enum UserStatus {
        ONLINE, OFFLINE, AWAY
    }
}
