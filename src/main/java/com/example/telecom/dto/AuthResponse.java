package com.example.telecom.dto;

import com.example.telecom.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    private Role role;
}