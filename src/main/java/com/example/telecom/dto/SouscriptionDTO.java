package com.example.telecom.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
    @Data
    @Builder
    public class SouscriptionDTO {
        private Long id;
        private LocalDate dateSouscription;
        private String statut;

        private PromotionSummary promotion;

        @Data
        @Builder
        public static class PromotionSummary {
            private Long id;
            private String nomPromotion;
        }
    }
