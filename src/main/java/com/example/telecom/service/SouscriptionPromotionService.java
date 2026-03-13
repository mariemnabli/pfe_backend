package com.example.telecom.service;

import com.example.telecom.entity.Contrat;
import com.example.telecom.entity.Promotion;
import com.example.telecom.entity.SouscriptionPromotion;
import com.example.telecom.repository.ContratRepository;
import com.example.telecom.repository.PromotionRepository;
import com.example.telecom.repository.SouscriptionPromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SouscriptionPromotionService {

    private final SouscriptionPromotionRepository souscriptionRepository;
    private final ContratRepository contratRepository;
    private final PromotionRepository promotionRepository;

    // Vente: Vérifier éligibilité et souscrire
    public SouscriptionPromotion souscrire(Long contratId, Long promotionId) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable"));

        verifierEligibilite(contrat, promotion);

        if (souscriptionRepository.existsByContratIdAndPromotionId(contratId, promotionId)) {
            throw new RuntimeException("Ce contrat bénéficie déjà de cette promotion");
        }

        SouscriptionPromotion souscription = SouscriptionPromotion.builder()
                .contrat(contrat)
                .promotion(promotion)
                .dateSouscription(LocalDate.now())
                .statut(SouscriptionPromotion.StatutSouscription.ACTIVE)
                .build();

        return souscriptionRepository.save(souscription);
    }

    public boolean verifierEligibilite(Contrat contrat, Promotion promotion) {
        // Vérifier statut promotion
        if (promotion.getStatut() != Promotion.StatutPromotion.ACTIVE) {
            throw new RuntimeException("La promotion n'est pas active");
        }

        // Vérifier période de validité
        LocalDate today = LocalDate.now();
        if (today.isBefore(promotion.getDateDebut()) || today.isAfter(promotion.getDateFin())) {
            throw new RuntimeException("La promotion est hors période de validité");
        }

        // Vérifier statut contrat
        if (contrat.getStatut() != Contrat.StatutContrat.ACTIF) {
            throw new RuntimeException("Le contrat n'est pas actif");
        }

        // Vérifier ancienneté minimale
        if (promotion.getAncienneteMinimale() != null && contrat.getDateDebut() != null) {
            long moisAnciennete = ChronoUnit.MONTHS.between(contrat.getDateDebut(), today);
            if (moisAnciennete < promotion.getAncienneteMinimale()) {
                throw new RuntimeException("Ancienneté insuffisante. Requis: "
                        + promotion.getAncienneteMinimale() + " mois, actuel: " + moisAnciennete + " mois");
            }
        }

        return true;
    }

    public List<SouscriptionPromotion> getSouscriptionsByContrat(Long contratId) {
        return souscriptionRepository.findByContratId(contratId);
    }

    public List<SouscriptionPromotion> getSouscriptionsByPromotion(Long promotionId) {
        return souscriptionRepository.findByPromotionId(promotionId);
    }
}