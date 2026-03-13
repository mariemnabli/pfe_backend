package com.example.telecom.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceDTO {
    private Long id;
    private String nomService;
    private String description;
}