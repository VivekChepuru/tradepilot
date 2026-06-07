package com.tradepilot.core.security;

import com.tradepilot.core.entity.User;
import com.tradepilot.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads a Spring Security UserDetails by phone number (used as username).
     * Expects passwords in DB to be BCrypt-encoded (i.e., stored via passwordEncoder.encode()).
     */
    @Override
    public UserDetails loadUserByUsername(String phoneNumber) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone: " + phoneNumber));

        // Build Spring Security's UserDetails using the phoneNumber and encoded password.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPassword())  // must be encoded
                .roles("USER")
                .build();
    }
}
