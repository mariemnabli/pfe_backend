package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Format : 000001, 000002, 000003 ...
    @Column(name = "customer_id", unique = true, updatable = false)
    private String customerId;

    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
    private String ville;

    // 1 = CIN  |  2 = PASSPORT
    @Column(name = "document_type", nullable = false)
    private Integer documentType;

    // ── Champs CIN (documentType = 1) ─────────────────────────
    @Column(unique = true)
    private String cinNumber;

    @Column(name = "cin_image_path")
    private String cinImagePath;

    // ── Champs PASSPORT (documentType = 2) ────────────────────
    @Column(unique = true)
    private String passportNumber;

    @Column(name = "passport_image_path")
    private String passportImagePath;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Contrat> contrats;
}