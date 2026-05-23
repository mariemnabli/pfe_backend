// CustomerPromotionDateController.java
package com.example.telecom.controller;

import com.example.telecom.dto.CustomerPromotionDateDTO;
import com.example.telecom.dto.BulkCustomerDateUpdateDTO;
import com.example.telecom.dto.CustomerDateUpdateResponseDTO;
import com.example.telecom.service.CustomerPromotionDateService;
import com.example.telecom.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotion-dates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CustomerPromotionDateController {

    private final CustomerPromotionDateService dateService;
    private final CustomerPromotionDateService CustomerPromotionService;

    /**
     * GET /api/promotion-dates/customers?promotionId=1&groupId=5
     * Obtenir la liste des clients avec leurs dates de promotion
     */
    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('EXPLOIT', 'METIER', 'VENTE')")
    public ResponseEntity<List<CustomerPromotionDateDTO>> getCustomersWithDates(
            @RequestParam Long promotionId,
            @RequestParam Long groupId) {

        List<CustomerPromotionDateDTO> customers = dateService.getCustomersWithPromotionDates(promotionId, groupId);
        return ResponseEntity.ok(customers);
    }

    /**
     * PUT /api/promotion-dates/customers/bulk
     * Mettre à jour les dates pour plusieurs clients
     */
    @PutMapping("/customers/bulk")
    @PreAuthorize("hasAnyRole('EXPLOIT', 'METIER')")
    public ResponseEntity<CustomerDateUpdateResponseDTO> bulkUpdateDates(
            @RequestBody BulkCustomerDateUpdateDTO request,
            @RequestParam Long userId) {

        CustomerDateUpdateResponseDTO response = dateService.updateDatesForCustomers(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/promotion-dates/customers/{customerId}
     * Mettre à jour les dates pour un client spécifique
     */
// Dans PromotionDatesController.java
    @PutMapping("/customers/{customerId}")
    public ResponseEntity<?> updateCustomerDates(
            @PathVariable Long customerId,
            @RequestBody Map<String, Object> request) {

        try {
            Long promotionId = Long.valueOf(request.get("promotionId").toString());
            String startDateStr = (String) request.get("startDate");
            String endDateStr = (String) request.get("endDate");
            Long groupId = request.get("groupId") != null ?
                    Long.valueOf(request.get("groupId").toString()) : null;
            Long userId = Long.valueOf(request.get("userId").toString());

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : null;

            // Appeler le service
            CustomerPromotionService.updateCustomerDates(promotionId, customerId, startDate, endDate, groupId, userId);

            // Retourner une réponse avec les données mises à jour
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Dates mises à jour avec succès");
            response.put("customerId", customerId);
            response.put("promotionId", promotionId);
            response.put("startDate", startDate);
            response.put("endDate", endDate);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * DELETE /api/promotion-dates/customers/{customerId}/reset
     * Réinitialiser les dates d'un client
     */
    @PutMapping("/customers/{customerId}/reset")
    @PreAuthorize("hasAnyRole('EXPLOIT', 'METIER')")
    public ResponseEntity<?> resetCustomerDates(
            @PathVariable Long customerId,
            @RequestParam Long promotionId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long groupId,
            @RequestParam Long userId) {

        try {
            // Valider les dates
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La date de début doit être avant la date de fin"));
            }

            // Appeler le service avec groupId
            CustomerPromotionService.resetCustomerDates(promotionId, customerId, groupId);

            // Retourner la réponse mise à jour
            Map<String, Object> response = Map.of(
                    "message", "Dates réinitialisées avec succès",
                    "customerId", customerId,
                    "promotionId", promotionId,
                    "startDate", startDate,
                    "endDate", endDate
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            //log.error("Erreur lors de la réinitialisation des dates", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/promotion-dates/groups/{groupId}/apply-to-all
     * Appliquer les mêmes dates à tous les membres d'un groupe
     */
    @PostMapping("/groups/{groupId}/apply-to-all")
    @PreAuthorize("hasAnyRole('EXPLOIT', 'METIER')")
    public ResponseEntity<CustomerDateUpdateResponseDTO> applyToAllMembers(
            @PathVariable Long groupId,
            @RequestParam Long promotionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Long userId) {

        CustomerDateUpdateResponseDTO response = dateService.applyDatesToAllMembers(
                promotionId, groupId, startDate, endDate, userId);
        return ResponseEntity.ok(response);
    }
}