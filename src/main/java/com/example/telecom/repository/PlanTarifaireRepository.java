package com.example.telecom.repository;

import com.example.telecom.entity.PlanTarifaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanTarifaireRepository extends JpaRepository<PlanTarifaire, Long> {
    List<PlanTarifaire> findAllByOrderByPrixMensuelAsc();
}
