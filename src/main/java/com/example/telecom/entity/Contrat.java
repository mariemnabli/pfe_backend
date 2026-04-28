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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractType contractType = ContractType.INDIVIDUAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractHolderType holderType = ContractHolderType.CUSTOMER;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutContrat statut = StatutContrat.ACTIF;

    private Number directoryNumber;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "customer_group_id")
    private CustomerGroup customerGroup;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private Offre offre;


    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<SouscriptionPromotion> souscriptions;

    @OneToMany(mappedBy = "targetContract", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PromotionAssignment> promotionAssignments;

    public enum StatutContrat {
        ACTIF, RESILIE, SUSPENDU
    }


}
