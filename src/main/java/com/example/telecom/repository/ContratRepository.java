package com.example.telecom.repository;

import com.example.telecom.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByClientId(Long clientId);
    List<Contrat> findByStatut(Contrat.StatutContrat statut);
}