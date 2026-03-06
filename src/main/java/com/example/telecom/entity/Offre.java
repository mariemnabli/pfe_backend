package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "offres")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomOffre;
    private String typeOffre;

    @ManyToOne
    @JoinColumn(name = "plan_tarifaire_id")
    private PlanTarifaire planTarifaire;

    @ManyToMany
    @JoinTable(
            name = "offre_services",
            joinColumns = @JoinColumn(name = "offre_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<Services> services;
}