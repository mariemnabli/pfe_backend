package com.example.telecom.dto;

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

    // 1 = CIN  |  2 = PASSPORT
    private Integer documentType;

    // Champs CIN (documentType = 1)
    private String cinNumber;
    private String cinImagePath;

    // Champs PASSPORT (documentType = 2)
    private String passportNumber;
    private String passportImagePath;
}