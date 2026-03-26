package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private StatutReclamation statut = StatutReclamation.OUVERTE;

    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;

    private String commentaireVendeur;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @PrePersist
    public void prePersist() {
        dateCreation  = LocalDateTime.now();
        dateMiseAJour = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        dateMiseAJour = LocalDateTime.now();
    }

    public enum StatutReclamation {
        OUVERTE, EN_COURS, FERMEE
    }
}