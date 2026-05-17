package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "directory_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectoryNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true)
    private Long numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DirectoryNumberStatus status;

    @Column(name = "date_activation")
    private LocalDate dateActivation;

    @Column(name = "date_desactivation")
    private LocalDate dateDesactivation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    public enum DirectoryNumberStatus {
        LIBRE,
        ACTIF,
        DESACTIVE
    }
}
