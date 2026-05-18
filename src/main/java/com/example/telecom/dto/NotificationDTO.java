package com.example.telecom.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private String resourceType;
    private Long resourceId;
    private LocalDateTime createdAt;
}
