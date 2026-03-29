package com.example.telecom.dto;

import com.example.telecom.entity.Role;
import lombok.*;
import java.util.Date;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean enabled;
    private boolean premiereConnexion;
    private Date firstTimeConnexion;
}