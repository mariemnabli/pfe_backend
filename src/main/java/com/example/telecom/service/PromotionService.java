package com.example.telecom.service;

import com.example.telecom.dto.PromotionDTO;
import com.example.telecom.entity.Promotion;
import com.example.telecom.entity.User;
import com.example.telecom.repository.PromotionRepository;
import com.example.telecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserRepository userRepository;

    // Métier: Créer une promotion
    public PromotionDTO creerPromotion(PromotionDTO dto) {
        User createur = userRepository.findById(dto.getCreateurId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Promotion promotion = Promotion.builder()
                .nomPromotion(dto.getNomPromotion())
                .typeReduction(dto.getTypeReduction())
                .valeurReduction(dto.getValeurReduction())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .regleEligibilite(dto.getRegleEligibilite())
                .ancienneteMinimale(dto.getAncienneteMinimale())
                .statut(Promotion.StatutPromotion.EN_ATTENTE)
                .createur(createur)
                .build();

        return toDTO(promotionRepository.save(promotion));
    }

    // Exploit: Valider une promotion
    public PromotionDTO validerPromotion(Long id, Long validateurId) {
        Promotion promotion = getPromotion(id);
        User validateur = userRepository.findById(validateurId)
                .orElseThrow(() -> new RuntimeException("Validateur introuvable"));
        promotion.setStatut(Promotion.StatutPromotion.VALIDEE);
        promotion.setValidateur(validateur);
        return toDTO(promotionRepository.save(promotion));
    }

    // Exploit: Rejeter une promotion
    public PromotionDTO rejeterPromotion(Long id, Long validateurId) {
        Promotion promotion = getPromotion(id);
        User validateur = userRepository.findById(validateurId)
                .orElseThrow(() -> new RuntimeException("Validateur introuvable"));
        promotion.setStatut(Promotion.StatutPromotion.REJETEE);
        promotion.setValidateur(validateur);
        return toDTO(promotionRepository.save(promotion));
    }

    // Exploit: Activer une promotion
    public PromotionDTO activerPromotion(Long id) {
        Promotion promotion = getPromotion(id);
        if (promotion.getStatut() != Promotion.StatutPromotion.VALIDEE
                && promotion.getStatut() != Promotion.StatutPromotion.SUSPENDUE) {
            throw new RuntimeException("La promotion doit être validée avant activation");
        }
        promotion.setStatut(Promotion.StatutPromotion.ACTIVE);
        return toDTO(promotionRepository.save(promotion));
    }

    // Exploit: Suspendre une promotion
    public PromotionDTO suspendrePromotion(Long id) {
        Promotion promotion = getPromotion(id);
        promotion.setStatut(Promotion.StatutPromotion.SUSPENDUE);
        return toDTO(promotionRepository.save(promotion));
    }

    public List<PromotionDTO> getAll() {
        return promotionRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<PromotionDTO> getByStatut(Promotion.StatutPromotion statut) {
        return promotionRepository.findByStatut(statut).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PromotionDTO getById(Long id) {
        return toDTO(getPromotion(id));
    }

    private Promotion getPromotion(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable : " + id));
    }

    private PromotionDTO toDTO(Promotion p) {
        return PromotionDTO.builder()
                .id(p.getId())
                .nomPromotion(p.getNomPromotion())
                .typeReduction(p.getTypeReduction())
                .valeurReduction(p.getValeurReduction())
                .dateDebut(p.getDateDebut())
                .dateFin(p.getDateFin())
                .statut(p.getStatut())
                .regleEligibilite(p.getRegleEligibilite())
                .ancienneteMinimale(p.getAncienneteMinimale())
                .createurId(p.getCreateur()   != null ? p.getCreateur().getId()   : null)
                .validateurId(p.getValidateur() != null ? p.getValidateur().getId() : null)
                // ✅ objets enrichis
                .createur(p.getCreateur() != null ? PromotionDTO.UserSummary.builder()
                        .id(p.getCreateur().getId())
                        .username(p.getCreateur().getUsername())
                        .email(p.getCreateur().getEmail())
                        .role(p.getCreateur().getRole().name())
                        .build() : null)
                .validateur(p.getValidateur() != null ? PromotionDTO.UserSummary.builder()
                        .id(p.getValidateur().getId())
                        .username(p.getValidateur().getUsername())
                        .email(p.getValidateur().getEmail())
                        .role(p.getValidateur().getRole().name())
                        .build() : null)
                .build();
    }
}