package com.tradepilot.core.dto.response;

import lombok.Data;

import java.util.UUID;
@Data
public class UserResponse {
    private UUID id;
    private UUID userId;
    private String username;
    private String phoneNumber;
    private String userStatus;
}
