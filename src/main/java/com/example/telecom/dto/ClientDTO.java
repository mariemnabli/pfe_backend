package com.example.telecom.dto;

import java.time.LocalDate;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientDTO {
    private Long id;
    private String customerId;       // généré automatiquement, lecture seule
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
    private String ville;
    private String status;
    private LocalDate dateActivation;
    private LocalDate dateDesactivation;

    // 1 = CIN  |  2 = PASSPORT
    private Integer documentType;

    // Champs CIN (documentType = 1)
    private String cinNumber;
    private String cinImagePath;

    // Champs PASSPORT (documentType = 2)
    private String passportNumber;
    private String passportImagePath;

    private Long customerGroupId;
    private GroupSummary customerGroup;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GroupSummary {
        private Long id;
        private String groupCode;
        private String name;
    }
}
