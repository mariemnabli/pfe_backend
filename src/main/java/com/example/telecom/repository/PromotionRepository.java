package com.example.telecom.repository;

import com.example.telecom.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByStatut(Promotion.StatutPromotion statut);
    List<Promotion> findByCreateurId(Long createurId);
}