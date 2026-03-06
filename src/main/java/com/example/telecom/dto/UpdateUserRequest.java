package com.example.telecom.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String username;
    private String email;
    private String role;
    private String password;
    private String actif;
}