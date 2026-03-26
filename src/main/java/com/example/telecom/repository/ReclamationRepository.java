package com.example.telecom.repository;

import com.example.telecom.entity.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    List<Reclamation> findByClientId(Long clientId);
    List<Reclamation> findByStatut(Reclamation.StatutReclamation statut);
}