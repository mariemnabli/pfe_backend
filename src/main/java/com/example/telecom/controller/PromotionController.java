package com.example.telecom.controller;

import com.example.telecom.dto.PromotionAssignmentDTO;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.dto.PromotionDTO;
import com.example.telecom.entity.Promotion;
import com.example.telecom.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<PromotionDTO> creer(@RequestBody PromotionDTO dto) {
        return ResponseEntity.ok(promotionService.creerPromotion(dto));
    }

    // Exploit: Valider
    @PutMapping("/{id}/valider")
    @PreAuthorize("hasRole('EXPLOIT')")
    public ResponseEntity<PromotionDTO> valider(@PathVariable Long id,
                                                @RequestParam Long validateurId) {
        return ResponseEntity.ok(promotionService.validerPromotion(id, validateurId));
    }

    // Exploit: Rejeter
    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasRole('EXPLOIT')")
    public ResponseEntity<PromotionDTO> rejeter(@PathVariable Long id,
                                                @RequestParam Long validateurId) {
        return ResponseEntity.ok(promotionService.rejeterPromotion(id, validateurId));
    }

    // Exploit: Activer
    @PutMapping("/{id}/activer")
    @PreAuthorize("hasRole('EXPLOIT')")
    public ResponseEntity<PromotionDTO> activer(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.activerPromotion(id));
    }

    // Exploit: Suspendre
    @PutMapping("/{id}/suspendre")
    @PreAuthorize("hasRole('EXPLOIT')")
    public ResponseEntity<PromotionDTO> suspendre(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.suspendrePromotion(id));
    }

    // Métier: Modifier une promotion
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<PromotionDTO> modifier(@PathVariable Long id,
                                                 @RequestBody PromotionDTO dto) {
        return ResponseEntity.ok(promotionService.modifierPromotion(id, dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('METIER','EXPLOIT','VENTE')")
    public ResponseEntity<PaginatedResponse<PromotionDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(promotionService.getAll(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('METIER','EXPLOIT','VENTE')")
    public ResponseEntity<PromotionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getById(id));
    }

    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('METIER','EXPLOIT')")
    public ResponseEntity<List<PromotionDTO>> getByStatut(@PathVariable Promotion.StatutPromotion statut) {
        return ResponseEntity.ok(promotionService.getByStatut(statut));
    }

    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<PromotionAssignmentDTO> assignerPromotion(@PathVariable Long id,
                                                                    @RequestBody PromotionAssignmentDTO dto) {
        return ResponseEntity.ok(promotionService.assignerPromotion(id, dto));
    }

    @PutMapping("/{id}/assignments/{assignmentId}/valider")
    @PreAuthorize("hasRole('EXPLOIT')")
    public ResponseEntity<PromotionAssignmentDTO> validerAssignment(@PathVariable Long id,
                                                                    @PathVariable Long assignmentId,
                                                                    @RequestParam Long validateurId) {
        return ResponseEntity.ok(promotionService.validerAssignment(id, assignmentId, validateurId));
    }

    @PutMapping("/{id}/assignments/{assignmentId}/rejeter")
    @PreAuthorize("hasRole('EXPLOIT')")
    public ResponseEntity<PromotionAssignmentDTO> rejeterAssignment(@PathVariable Long id,
                                                                    @PathVariable Long assignmentId,
                                                                    @RequestParam Long validateurId) {
        return ResponseEntity.ok(promotionService.rejeterAssignment(id, assignmentId, validateurId));
    }

    @GetMapping("/{id}/assignments")
    @PreAuthorize("hasAnyRole('METIER','EXPLOIT','VENTE')")
    public ResponseEntity<List<PromotionAssignmentDTO>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getAssignmentsByPromotion(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<List<PromotionDTO>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(promotionService.getPromotionsApplicablesAuClient(customerId));
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<List<PromotionDTO>> getByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(promotionService.getPromotionsApplicablesAuGroupe(groupId));
    }
}
