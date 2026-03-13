package com.example.telecom.repository;

import com.example.telecom.entity.SouscriptionPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SouscriptionPromotionRepository extends JpaRepository<SouscriptionPromotion, Long> {
    List<SouscriptionPromotion> findByContratId(Long contratId);
    List<SouscriptionPromotion> findByPromotionId(Long promotionId);
    boolean existsByContratIdAndPromotionId(Long contratId, Long promotionId);
}