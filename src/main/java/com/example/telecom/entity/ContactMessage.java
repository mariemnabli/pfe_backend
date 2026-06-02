package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "contact_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false, length = 200)
    private String sujet;

    @Column(nullable = false, length = 3000)
    private String message;

    @Column(length = 3000)
    private String reponseDsi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatutContact statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    private LocalDateTime dateMiseAJour;

    private LocalDateTime dateReponse;

    @PrePersist
    public void prePersist() {
        LocalDateTime maintenant = LocalDateTime.now();
        dateCreation = maintenant;
        dateMiseAJour = maintenant;
        if (statut == null) {
            statut = StatutContact.NOUVEAU;
        }
    }

    @PreUpdate
    public void preUpdate() {
        dateMiseAJour = LocalDateTime.now();
    }

    public enum StatutContact {
        NOUVEAU,
        REPONDU
    }
}
