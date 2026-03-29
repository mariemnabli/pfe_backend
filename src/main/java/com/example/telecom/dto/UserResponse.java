package com.example.telecom.dto;

import com.example.telecom.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String actif;

    public static UserResponse fromEntity(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setEmail(user.getEmail());
        res.setRole(String.valueOf(user.getRole()));
        res.setActif(String.valueOf(user.isEnabled()));
        return res;
    }
}