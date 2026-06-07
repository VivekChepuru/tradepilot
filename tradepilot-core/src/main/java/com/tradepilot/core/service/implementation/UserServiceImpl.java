package com.tradepilot.core.service.implementation;

import com.tradepilot.core.dto.request.UserRegisterRequest;
import com.tradepilot.core.dto.response.UserStatus;
import com.tradepilot.core.entity.User;
import com.tradepilot.core.repository.UserRepository;
import com.tradepilot.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; //Inject encoder

    @Override
    public User registerUser(UserRegisterRequest dto) {
        if (dto.getPhoneNumber() == null || !dto.getPhoneNumber().matches("\\d{10}")) {
            throw new IllegalArgumentException("Invalid phone number. Must be 10 digits.");
        }

        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered.");
        }
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Username already taken.");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPhoneNumber(dto.getPhoneNumber());

        // IMPORTANT: hash the password before saving
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // other fields (id, userId, userStatus) handled by @PrePersist in entity

        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUser(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public User updateUserStatus(UUID userId, UserStatus userStatus) {
        User user = userRepository.findById(userId)
                        .orElseThrow(()-> new IllegalArgumentException("User with ID " + userId + " not found."));
        user.setUserStatus(userStatus);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID userId) {
       if (!userRepository.existsById(userId)) {
           throw new IllegalArgumentException ("User with ID " + userId + " not found.");
       }
        userRepository.deleteById(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isPhoneNumberTaken(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }
}
