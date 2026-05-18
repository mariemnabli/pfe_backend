package com.example.telecom.dto;

import com.example.telecom.entity.Reclamation;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReclamationDTO {
    private Long id;
    private String description;
    private Reclamation.StatutReclamation statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private String commentaireVendeur;
    private String commentaireDsi;

    // input
    private Long clientId;
    private Long customerGroupId;

    // output enrichi
    private ClientInfo client;
    private CustomerGroupInfo customerGroup;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientInfo {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerGroupInfo {
        private Long id;
        private String groupCode;
        private String name;
        private String groupType;
        private String status;
    }
}
