package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "souscriptions_promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SouscriptionPromotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dateSouscription;

    @Enumerated(EnumType.STRING)
    private StatutSouscription statut = StatutSouscription.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    public enum StatutSouscription {
        ACTIVE, EXPIRÉE, ANNULÉE
    }
}