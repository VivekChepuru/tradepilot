package com.tradepilot.core.controller;

import com.tradepilot.core.dto.request.UserRegisterRequest;
import com.tradepilot.core.dto.response.UserResponse;
import com.tradepilot.core.dto.response.UserStatus;
import com.tradepilot.core.mapper.UserMapper;
import com.tradepilot.core.entity.User;
import com.tradepilot.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // REGISTER (public)
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest req) {
        User user = userService.registerUser(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(user));
    }

    // GET BY INTERNAL ID (UUID PK)
    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id).orElseThrow());
    }

    // GET BY PHONE
    @GetMapping("/phone/{phoneNumber}")
    public ResponseEntity<UserResponse> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        return userService.getUserByPhoneNumber(phoneNumber)
                .map(user -> ResponseEntity.ok(UserMapper.toResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE STATUS
    @PutMapping("/id/{id}/status")
    public ResponseEntity<User> updateUserStatus(
            @PathVariable UUID id,
            @RequestParam UserStatus userStatus) {
        return ResponseEntity.ok(userService.updateUserStatus(id, userStatus));
    }

    // DELETE USER
    @DeleteMapping("/id/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

}
