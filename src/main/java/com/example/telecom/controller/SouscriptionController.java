package com.example.telecom.controller;

import com.example.telecom.dto.SouscriptionDTO;
import com.example.telecom.entity.SouscriptionPromotion;
import com.example.telecom.service.SouscriptionPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/souscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SouscriptionController {

    private final SouscriptionPromotionService souscriptionService;

    // Vente: Souscrire une promotion pour un contrat
    @PostMapping("/contrat/{contratId}/promotion/{promotionId}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<SouscriptionPromotion> souscrire(
            @PathVariable Long contratId,
            @PathVariable Long promotionId) {
        return ResponseEntity.ok(souscriptionService.souscrire(contratId, promotionId));
    }

    // Vente: Vérifier éligibilité
    @GetMapping("/contrat/{contratId}/promotion/{promotionId}/eligibilite")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<String> verifierEligibilite(
            @PathVariable Long contratId,
            @PathVariable Long promotionId) {
        try {
            souscriptionService.verifierEligibilite(contratId, promotionId);
            return ResponseEntity.ok("Contrat éligible à la promotion");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Non éligible : " + e.getMessage());
        }
    }

    @GetMapping("/contrat/{contratId}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<List<SouscriptionDTO>> getByContrat(@PathVariable Long contratId) {
        return ResponseEntity.ok(souscriptionService.getSouscriptionsByContrat(contratId));
    }

    @GetMapping("/promotion/{promotionId}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<List<SouscriptionPromotion>> getByPromotion(@PathVariable Long promotionId) {
        return ResponseEntity.ok(souscriptionService.getSouscriptionsByPromotion(promotionId));
    }
}
