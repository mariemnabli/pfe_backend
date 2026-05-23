package com.example.telecom.service;

import com.example.telecom.dto.SouscriptionDTO;
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
    private final PromotionService promotionService;

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

        if (!promotionService.isPromotionAssignableToContrat(contrat, promotion)) {
            throw new RuntimeException("La promotion n'est pas affectee a ce client, groupe ou contrat");
        }

        return true;
    }

    public boolean verifierEligibilite(Long contratId, Long promotionId) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable"));
        return verifierEligibilite(contrat, promotion);
    }

    public List<SouscriptionDTO> getSouscriptionsByContrat(Long contratId) {
        return souscriptionRepository.findByContratId(contratId)
                .stream()
                .map(s -> SouscriptionDTO.builder()
                        .id(s.getId())
                        .dateSouscription(s.getDateSouscription())
                        .statut(s.getStatut().name())
                        .promotion(SouscriptionDTO.PromotionSummary.builder()
                                .id(s.getPromotion().getId())
                                .nomPromotion(s.getPromotion().getNomPromotion())
                                .build())
                        .build())
                .toList();
    }

    public List<SouscriptionPromotion> getSouscriptionsByPromotion(Long promotionId) {
        return souscriptionRepository.findByPromotionId(promotionId);
    }
}
