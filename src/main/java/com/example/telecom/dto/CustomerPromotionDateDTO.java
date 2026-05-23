package com.example.telecom.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPromotionDateDTO {
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Long contractId;
    private String contractNumber;
    private Long groupId;
    private String groupName;
    private Long promotionId;
    private String promotionName;
    private String promotionValue;
    private LocalDate currentStartDate;
    private LocalDate currentEndDate;
    private LocalDate newStartDate;
    private LocalDate newEndDate;
    private boolean isCustomized;
    private String period; // Calculé: "X mois" ou "Du DD/MM au DD/MM"
    private String status; // ACTIVE, EXPIRED, PENDING
}