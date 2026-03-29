package com.example.telecom.dto;

import com.example.telecom.entity.Promotion;
import lombok.*;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
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

    // input
    private Long createurId;
    private Long validateurId;

    // ✅ output enrichi
    private UserSummary createur;
    private UserSummary validateur;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserSummary {
        private Long   id;
        private String username;
        private String email;
        private String role;
    }
}