package com.tradepilot.core.mapper;

import com.tradepilot.core.dto.response.UserResponse;
import com.tradepilot.core.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setUserStatus(user.getUserStatus().name());
        return dto;
    }

}
