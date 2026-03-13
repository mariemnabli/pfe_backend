package com.example.telecom.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OffreDTO {
    private Long id;
    private String nomOffre;
    private String typeOffre;
    private Long planTarifaireId;
    private List<Long> serviceIds;
}