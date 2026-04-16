package com.example.telecom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "contract_id", unique = true, updatable = false)
    private String contractId;

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
    @JsonIgnore
    private List<SouscriptionPromotion> souscriptions;

    public enum StatutContrat {
        ACTIF, RESILIE, SUSPENDU
    }


}