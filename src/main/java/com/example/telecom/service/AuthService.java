package com.example.telecom.service;

import com.example.telecom.dto.*;
import com.example.telecom.entity.User;
import com.example.telecom.repository.UserRepository;
import com.example.telecom.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository      userRepository;
    private final PasswordEncoder     passwordEncoder;
    private final JwtUtil             jwtUtil;
    private final EmailService        emailService;
    private final RefreshTokenService refreshTokenService;

    // ─── LOGIN ───────────────────────────────────────────────
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // Première connexion
        if (!user.isPremiereConnexion()) {
            user.setPremiereConnexion(true);
            user.setEnabled(true);
            user.setFirstTimeConnexion(new Date());
            userRepository.save(user);
            emailService.envoyerNotificationPremiereConnexion(user.getEmail(), user.getUsername());
        }

        String accessToken  = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.generer(user); // ✅

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole());
    }

    // ─── REFRESH ─────────────────────────────────────────────
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokenService.valider(refreshToken);

        String newAccessToken  = jwtUtil.generateToken(user);
        String newRefreshToken = refreshTokenService.generer(user); // rotation

        return new AuthResponse(newAccessToken, newRefreshToken, user.getEmail(), user.getRole());
    }

    // ─── LOGOUT ──────────────────────────────────────────────
    public void logout(String refreshToken) {
        refreshTokenService.revoquer(refreshToken);
    }

    // ─── REGISTER ────────────────────────────────────────────
    public AuthResponse register(User user) {
        if (userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        String accessToken  = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.generer(user); // ✅

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole());
    }

    // ─── FORGOT PASSWORD ─────────────────────────────────────
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email non trouvé"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
        userRepository.save(user);

        emailService.envoyerResetPassword(user.getEmail(), token);
    }

    // ─── RESET PASSWORD ──────────────────────────────────────
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}