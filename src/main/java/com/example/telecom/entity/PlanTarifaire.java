package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plans_tarifaires")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PlanTarifaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private Double prixMensuel;
    private String description;
}