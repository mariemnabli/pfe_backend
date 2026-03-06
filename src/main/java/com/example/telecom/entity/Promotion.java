package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomPromotion;
    private String typeReduction;   // POURCENTAGE, MONTANT_FIXE
    private Double valeurReduction;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    private StatutPromotion statut = StatutPromotion.EN_ATTENTE;

    // Règles d'éligibilité (ex: type offre requis, ancienneté minimale en mois)
    private String regleEligibilite;
    private Integer ancienneteMinimale; // en mois

    @ManyToOne
    @JoinColumn(name = "createur_id")
    private User createur;

    @ManyToOne
    @JoinColumn(name = "validateur_id")
    private User validateur;

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL)
    private List<SouscriptionPromotion> souscriptions;

    public enum StatutPromotion {
        EN_ATTENTE, VALIDEE, REJETEE, ACTIVE, SUSPENDUE
    }
}