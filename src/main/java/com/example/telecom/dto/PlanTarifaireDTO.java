package com.example.telecom.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanTarifaireDTO {
    private Long id;
    private String nom;
    private Double prixMensuel;
    private String description;
}