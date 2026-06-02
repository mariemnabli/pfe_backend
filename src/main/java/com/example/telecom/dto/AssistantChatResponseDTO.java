package com.example.telecom.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssistantChatResponseDTO {
    private String answer;
    private int offersUsed;
    private int plansUsed;
    private int servicesUsed;
    private String model;
}
