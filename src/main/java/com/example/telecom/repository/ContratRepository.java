package com.example.telecom.repository;

import com.example.telecom.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {
    List<Contrat> findByClientId(Long clientId);

    List<Contrat> findByCustomerGroupId(Long customerGroupId);

    List<Contrat> findByStatut(Contrat.StatutContrat statut);

    Optional<Contrat> findByContractId(String contractId);

    List<Contrat> findByClientIdAndStatut(Long clientId, Contrat.StatutContrat statut);

    @Query("SELECT MAX(c.id) FROM Contrat c")
    Optional<Long> findMaxId();
}
