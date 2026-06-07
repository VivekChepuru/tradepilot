package com.tradepilot.core.mapper;

import com.tradepilot.core.dto.response.MessageResponse;
import com.tradepilot.core.entity.Message;

public class MessageMapper {
    public static MessageResponse toResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setChatId(message.getChat().getId());
        response.setContent(message.getContent());
        response.setTimestamp(message.getTimestamp());

        // Sender info
        response.setSenderId(message.getSender().getId());
        response.setSenderUsername(message.getSender().getUsername());
        response.setSenderPhoneNumber(message.getSender().getPhoneNumber());

        return response;
    }
}
