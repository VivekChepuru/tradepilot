package com.tradepilot.core.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class ChatCreateRequest {

    private String chatName;

    @NotNull
    private Boolean isGroup;

    @NotEmpty
    private List<UUID> participantIds;  // Jackson will deserialize string UUIDs to UUID objects
}
