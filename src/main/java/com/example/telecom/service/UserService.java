package com.example.telecom.service;

import com.example.telecom.dto.UpdateUserRequest;
import com.example.telecom.dto.UserResponse;
import com.example.telecom.entity.Role;
import com.example.telecom.entity.User;
import com.example.telecom.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── GET ALL ──────────────────────────────────────────────
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ─── GET BY ID ────────────────────────────────────────────
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));
        return UserResponse.fromEntity(user);
    }

    // ─── SAVE (register) ─────────────────────────────────────
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // ─── UPDATE ───────────────────────────────────────────────
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'id : " + id));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(Role.valueOf(request.getRole()));
        }
        if (request.getActif() != null && !request.getActif().isBlank()) {
            user.setActif(Boolean.parseBoolean(request.getActif()));
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    // ─── DELETE ───────────────────────────────────────────────
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'id : " + id);
        }
        userRepository.deleteById(id);
    }
}