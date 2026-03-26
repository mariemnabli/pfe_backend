package com.example.telecom.dto;

import com.example.telecom.entity.Reclamation;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReclamationDTO {
    private Long id;
    private String description;
    private Reclamation.StatutReclamation statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String commentaireVendeur;

    // input
    private Long clientId;

    // output enrichi
    private ClientInfo client;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ClientInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
    }
}