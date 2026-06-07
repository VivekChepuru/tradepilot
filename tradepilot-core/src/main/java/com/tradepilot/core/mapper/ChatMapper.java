package com.tradepilot.core.mapper;

import com.tradepilot.core.dto.response.ChatResponse;
import com.tradepilot.core.entity.Chat;
import com.tradepilot.core.entity.User;

import java.util.stream.Collectors;

public class ChatMapper {
    public static ChatResponse toResponse(Chat chat) {
        ChatResponse dto = new ChatResponse();
        dto.setId(chat.getId());
        dto.setChatName(chat.getChatName());
        dto.setGroup(chat.isGroup());

        dto.setParticipants(chat.getParticipants().stream()
                .map(ChatMapper::mapParticipant)
                .collect(Collectors.toList()));

        return dto;
    }

    private static ChatResponse.ParticipantDto mapParticipant(User user) {
        ChatResponse.ParticipantDto p = new ChatResponse.ParticipantDto();
        p.setUserId(user.getUserId());
        p.setUsername(user.getUsername());
        p.setPhoneNumber(user.getPhoneNumber());
        return p;
    }
}
