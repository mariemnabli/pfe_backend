package com.example.telecom.dto;

import com.example.telecom.entity.Role;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateUserRequest {
    private String username;
    private String email;
    private Role role;
    // Le DSI peut fournir un mot de passe manuellement,
    // sinon il sera généré automatiquement
    private String password;
}