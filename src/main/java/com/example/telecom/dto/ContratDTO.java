package com.example.telecom.dto;

import com.example.telecom.entity.Contrat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratDTO {
    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Contrat.StatutContrat statut;
    private Number directoryNumber;

    // Pour la création (input)
    private Long clientId;
    private Long offreId;

    // Pour la lecture (output enrichi)
    private ClientSummary client;
    private OffreSummary offre;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientSummary {
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
    public static class OffreSummary {
        private Long id;
        private String nom;
    }
}