package com.example.telecom.security;

import com.example.telecom.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {

    private final UserRepository userRepository;

    public UserSecurity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isOwner(Authentication authentication, Long userId) {
        String email = authentication.getName();
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(email))
                .orElse(false);
    }
}