package com.example.telecom.dto;

import com.example.telecom.entity.Promotion;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionDTO {
    private Long id;
    private String nomPromotion;
    private String typeReduction;
    private Double valeurReduction;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Promotion.StatutPromotion statut;
    private String regleEligibilite;
    private Integer ancienneteMinimale;
    private Long createurId;
}