package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    private boolean actif = false;

    private boolean premiereConnexion = false;

    private Date firstTimeConnexion;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;
    private boolean enabled = true; // utilisateur actif ou non
}