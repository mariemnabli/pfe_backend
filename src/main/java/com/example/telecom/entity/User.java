package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean enabled = false;
    private boolean premiereConnexion = false;
    private Date firstTimeConnexion;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // ✅ Refresh token — intégré directement dans User
    @Column(unique = true)
    private String refreshToken;

    private Instant refreshTokenExpiry;

    // ── Helpers ──────────────────────────────────────────────
    public boolean hasValidRefreshToken(String token) {
        return token != null
                && token.equals(this.refreshToken)
                && this.refreshTokenExpiry != null
                && Instant.now().isBefore(this.refreshTokenExpiry);
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
    }
}