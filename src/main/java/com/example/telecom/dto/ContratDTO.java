package com.example.telecom.dto;

import com.example.telecom.entity.Contrat;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContratDTO {
    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Contrat.StatutContrat statut;
    private Long clientId;
    private Long offreId;
}