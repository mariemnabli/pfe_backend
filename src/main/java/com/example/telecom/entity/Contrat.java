package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "contrats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutContrat statut = StatutContrat.ACTIF;

    private Number directoryNumber;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private Offre offre;


    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL)
    private List<SouscriptionPromotion> souscriptions;

    public enum StatutContrat {
        ACTIF, RESILIE, SUSPENDU
    }


}