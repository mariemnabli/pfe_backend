package com.example.telecom.controller;

import com.example.telecom.dto.PromotionDTO;
import com.example.telecom.entity.Promotion;
import com.example.telecom.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromotionController {

    private final PromotionService promotionService;

    // Métier: Créer
    @PostMapping
    public ResponseEntity<PromotionDTO> creer(@RequestBody PromotionDTO dto) {
        return ResponseEntity.ok(promotionService.creerPromotion(dto));
    }

    // Exploit: Valider
    @PutMapping("/{id}/valider")
    public ResponseEntity<PromotionDTO> valider(@PathVariable Long id,
                                                @RequestParam Long validateurId) {
        return ResponseEntity.ok(promotionService.validerPromotion(id, validateurId));
    }

    // Exploit: Rejeter
    @PutMapping("/{id}/rejeter")
    public ResponseEntity<PromotionDTO> rejeter(@PathVariable Long id,
                                                @RequestParam Long validateurId) {
        return ResponseEntity.ok(promotionService.rejeterPromotion(id, validateurId));
    }

    // Exploit: Activer
    @PutMapping("/{id}/activer")
    public ResponseEntity<PromotionDTO> activer(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.activerPromotion(id));
    }

    // Exploit: Suspendre
    @PutMapping("/{id}/suspendre")
    public ResponseEntity<PromotionDTO> suspendre(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.suspendrePromotion(id));
    }

    @GetMapping
    public ResponseEntity<List<PromotionDTO>> getAll() {
        return ResponseEntity.ok(promotionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromotionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<PromotionDTO>> getByStatut(@PathVariable Promotion.StatutPromotion statut) {
        return ResponseEntity.ok(promotionService.getByStatut(statut));
    }
}