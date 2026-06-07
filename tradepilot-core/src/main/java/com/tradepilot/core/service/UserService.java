package com.tradepilot.core.service;

import com.tradepilot.core.dto.request.UserRegisterRequest;
import com.tradepilot.core.dto.response.UserStatus;
import com.tradepilot.core.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User registerUser(UserRegisterRequest dto);

    Optional<User> getUser(UUID userId);

    Optional<User> getUserByPhoneNumber(String phoneNumber);

    User updateUserStatus(UUID userId, UserStatus userStatus);

    void deleteUser(UUID userId);

    List<User> getAllUsers();

    boolean isUsernameTaken(String username);

    boolean isPhoneNumberTaken(String phoneNumber);
}
