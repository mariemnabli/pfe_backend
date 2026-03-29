package com.example.telecom.service;

import com.example.telecom.entity.User;
import com.example.telecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // 7 jours
    private long refreshExpirationMs;

    private final UserRepository userRepository;

    // ── Générer et sauvegarder un nouveau refresh token ──────
    @Transactional
    public String generer(User user) {
        String token = UUID.randomUUID().toString();
        user.setRefreshToken(token);
        user.setRefreshTokenExpiry(Instant.now().plusMillis(refreshExpirationMs));
        userRepository.save(user);
        return token;
    }

    // ── Valider et retourner le User associé ──────────────────
    public User valider(String token) {
        User user = userRepository.findByRefreshToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token introuvable"));

        if (!user.hasValidRefreshToken(token)) {
            user.clearRefreshToken();
            userRepository.save(user);
            throw new RuntimeException("Refresh token expiré — veuillez vous reconnecter");
        }

        return user;
    }

    // ── Révoquer (logout) ────────────────────────────────────
    @Transactional
    public void revoquer(String token) {
        userRepository.findByRefreshToken(token).ifPresent(user -> {
            user.clearRefreshToken();
            userRepository.save(user);
        });
    }
}