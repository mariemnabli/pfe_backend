// CustomerPromotionDateService.java
package com.example.telecom.service;

import com.example.telecom.dto.CustomerPromotionDateDTO;
import com.example.telecom.dto.BulkCustomerDateUpdateDTO;
import com.example.telecom.dto.CustomerDateUpdateResponseDTO;
import com.example.telecom.entity.*;
import com.example.telecom.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerPromotionDateService {

    private final PromotionAssignmentRepository promotionAssignmentRepository;
    private final PromotionRepository promotionRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final CustomerGroupMemberRepository customerGroupMemberRepository;
    private final ClientRepository clientRepository;
    private final ContratRepository contratRepository;
    private final UserRepository userRepository;

    /**
     * Obtenir la liste des clients d'un groupe avec leurs dates de promotion
     */
// Dans CustomerPromotionDateService.java - Corriger la méthode getCustomersWithPromotionDates
    public List<CustomerPromotionDateDTO> getCustomersWithPromotionDates(Long promotionId, Long groupId) {
        List<CustomerPromotionDateDTO> result = new ArrayList<>();

        // Récupérer la promotion
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion non trouvée"));

        // Récupérer le groupe
        CustomerGroup group = customerGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe non trouvé"));

        // Récupérer tous les membres actifs du groupe
        List<CustomerGroupMember> members = customerGroupMemberRepository
                .findByCustomerGroupIdAndStatus(groupId, CustomerGroupMember.MembershipStatus.ACTIVE);

        LocalDate today = LocalDate.now();

        for (CustomerGroupMember member : members) {
            Client client = member.getCustomer();

            // Chercher le contrat actif du client
            Contrat contrat = contratRepository.findByClientIdAndStatut(client.getId(), Contrat.StatutContrat.ACTIF)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (contrat == null) continue;

            // Récupérer le premier numéro de téléphone du contrat
            String directoryNumber = null;
            if (contrat.getDirectoryNumbers() != null && !contrat.getDirectoryNumbers().isEmpty()) {
                DirectoryNumber dn = contrat.getDirectoryNumbers().get(0);
                directoryNumber = String.valueOf(dn.getNumero()); // ou dn.getDirectoryNumber() selon votre entité
            }

            // Chercher l'assignation de promotion pour ce client
            List<PromotionAssignment> assignments = promotionAssignmentRepository
                    .findAllByPromotionIdAndTargetCustomerId(promotionId, client.getId());

            PromotionAssignment assignment = assignments.stream()
                    .filter(a -> a.getTargetType() == PromotionAssignment.TargetType.CUSTOMER)
                    .max(Comparator.comparing(PromotionAssignment::getId))
                    .orElseGet(() -> assignments.stream()
                            .filter(a -> a.getTargetType() == PromotionAssignment.TargetType.CUSTOMER_GROUP)
                            .max(Comparator.comparing(PromotionAssignment::getId))
                            .orElse(null));

            if (assignment != null) {
                LocalDate startDate = assignment.getEffectiveStartDate();
                LocalDate endDate = assignment.getEffectiveEndDate();

                // Calculer la période
                String period = calculatePeriod(startDate, endDate);

                // Déterminer le statut
                String status = determineStatus(startDate, endDate, today);

                result.add(CustomerPromotionDateDTO.builder()
                        .customerId(client.getId())
                        .customerName(client.getNom() + " " + client.getPrenom())
                        .customerEmail(client.getEmail())
                        .contractId(contrat.getId())
                        .contractNumber(directoryNumber) // Utiliser le numéro de téléphone
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .promotionId(promotion.getId())
                        .promotionName(promotion.getNomPromotion())
                        .promotionValue(formatPromotionValue(promotion))
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .newStartDate(startDate)
                        .newEndDate(endDate)
                        .isCustomized(assignment.getAssignmentMode() == PromotionAssignment.AssignmentMode.MANUAL)
                        .period(period)
                        .status(status)
                        .build());
            }
        }

        return result;
    }

    /**
     * Mettre à jour les dates pour plusieurs clients d'un coup
     */
    @Transactional
    public CustomerDateUpdateResponseDTO updateDatesForCustomers(BulkCustomerDateUpdateDTO request, Long userId) {

        log.info("Mise à jour des dates pour {} clients de la promotion {}",
                request.getCustomerIds().size(), request.getPromotionId());

        CustomerDateUpdateResponseDTO response = CustomerDateUpdateResponseDTO.builder()
                .errors(new ArrayList<>())
                .build();

        Promotion promotion = promotionRepository.findById(request.getPromotionId())
                .orElseThrow(() -> new RuntimeException("Promotion non trouvée"));

        // Validation des dates
        if (request.getNewStartDate() != null && request.getNewEndDate() != null
                && request.getNewEndDate().isBefore(request.getNewStartDate())) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }

        int successCount = 0;

        for (Long customerId : request.getCustomerIds()) {
            try {
                updateCustomerDates(
                        request.getPromotionId(),
                        customerId,
                        request.getNewStartDate(),
                        request.getNewEndDate(),
                        userId,
                        request.getGroupId()
                );
                successCount++;
            } catch (Exception e) {
                log.error("Erreur pour le client {}: {}", customerId, e.getMessage());
                Client client = clientRepository.findById(customerId).orElse(null);
                response.getErrors().add(CustomerDateUpdateResponseDTO.DateUpdateError.builder()
                        .customerId(customerId)
                        .customerName(client != null ? client.getNom() + " " + client.getPrenom() : "Inconnu")
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        response.setTotalProcessed(request.getCustomerIds().size());
        response.setSuccessCount(successCount);
        response.setFailedCount(response.getErrors().size());

        return response;
    }

    /**
     * Mettre à jour les dates pour un client spécifique
     */
    @Transactional
    public void updateCustomerDates(Long promotionId, Long customerId,
                                    LocalDate startDate, LocalDate endDate,
                                    Long groupId, Long userId) {

        List<PromotionAssignment> existingAssignments = promotionAssignmentRepository
                .findAllByPromotionIdAndTargetCustomerId(promotionId, customerId);

        if (!existingAssignments.isEmpty()) {
            // Garder le plus récent, supprimer les doublons
            PromotionAssignment latest = existingAssignments.stream()
                    .max(Comparator.comparing(PromotionAssignment::getId))
                    .get();

            existingAssignments.stream()
                    .filter(a -> !a.getId().equals(latest.getId()))
                    .forEach(promotionAssignmentRepository::delete);

            latest.setEffectiveStartDate(startDate);
            latest.setEffectiveEndDate(endDate);
            latest.setAssignmentMode(PromotionAssignment.AssignmentMode.MANUAL);
            // status reste PENDING — ne pas toucher
            promotionAssignmentRepository.save(latest);

        } else if (groupId != null) {
            List<PromotionAssignment> groupAssignments = promotionAssignmentRepository
                    .findByTargetCustomerId(promotionId);

            if (groupAssignments.isEmpty()) {
                throw new RuntimeException("Aucune assignation trouvée pour cette promotion et ce groupe");
            }

            PromotionAssignment groupAssignment = groupAssignments.stream()
                    .max(Comparator.comparing(PromotionAssignment::getId))
                    .get();

            // Créer une assignation individuelle PENDING pour ce client
            PromotionAssignment newAssignment = PromotionAssignment.builder()
                    .promotion(groupAssignment.getPromotion())
                    .targetType(PromotionAssignment.TargetType.CUSTOMER)
                    .targetCustomer(clientRepository.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Client non trouvé")))
                    .status(PromotionAssignment.AssignmentStatus.PENDING)      // ✅ PENDING
                    .validationStatus(PromotionAssignment.ValidationStatus.PENDING) // ✅ PENDING
                    .assignmentMode(PromotionAssignment.AssignmentMode.MANUAL)
                    .effectiveStartDate(startDate)
                    .effectiveEndDate(endDate)
                    .inheritedToMembers(false)
                    .assignedBy(userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé")))
                    .build();

            promotionAssignmentRepository.save(newAssignment);
        } else {
            throw new RuntimeException("Aucune assignation trouvée pour ce client");
        }
    }

    /**
     * Réinitialiser les dates d'un client aux dates par défaut de la promotion
     */
    @Transactional
    public void resetCustomerDates(Long promotionId, Long customerId, Long groupId) {
        // Chercher d'abord l'affectation individuelle du client
        Optional<PromotionAssignment> individualAssignment = promotionAssignmentRepository
                .findByPromotionIdAndTargetCustomerId(promotionId, customerId);

        // Chercher l'affectation du groupe si groupId est fourni
        Optional<PromotionAssignment> groupAssignment = Optional.empty();
        if (groupId != null) {
            groupAssignment = promotionAssignmentRepository
                    .findByPromotionIdAndTargetGroupId(promotionId, groupId);
        }

        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion non trouvée"));

        // Si une assignation individuelle existe (CUSTOMER), la supprimer
        if (individualAssignment.isPresent()) {
            PromotionAssignment assignment = individualAssignment.get();
            if (assignment.getTargetType() == PromotionAssignment.TargetType.CUSTOMER) {
                promotionAssignmentRepository.delete(assignment);
                log.info("Assignation personnalisée supprimée pour le client {}", customerId);
            }
        }

        // Si une assignation de groupe existe, réinitialiser ses dates
        if (groupAssignment.isPresent()) {
            PromotionAssignment assignment = groupAssignment.get();
            if (assignment.getTargetType() == PromotionAssignment.TargetType.CUSTOMER_GROUP) {
                assignment.setEffectiveStartDate(promotion.getDateDebut());
                assignment.setEffectiveEndDate(promotion.getDateFin());
                assignment.setAssignmentMode(PromotionAssignment.AssignmentMode.AUTOMATIC);
                promotionAssignmentRepository.save(assignment);
                log.info("Dates réinitialisées pour le client {} aux dates de la promotion ({})",
                        customerId, groupId);
            }
        }

        // Si aucune assignation trouvée
        if (!individualAssignment.isPresent() && !groupAssignment.isPresent()) {
            log.warn("Aucune assignation trouvée pour la promotion {} et le client {} (groupe {})",
                    promotionId, customerId, groupId);
            throw new RuntimeException("Aucune assignation trouvée pour cette promotion");
        }
    }

    /**
     * Appliquer les mêmes dates à tous les membres d'un groupe
     */
    @Transactional
    public CustomerDateUpdateResponseDTO applyDatesToAllMembers(Long promotionId, Long groupId,
                                                                LocalDate startDate, LocalDate endDate,
                                                                Long userId) {
        List<CustomerGroupMember> members = customerGroupMemberRepository
                .findByCustomerGroupIdAndStatus(groupId, CustomerGroupMember.MembershipStatus.ACTIVE);

        List<Long> customerIds = members.stream()
                .map(m -> m.getCustomer().getId())
                .collect(Collectors.toList());

        BulkCustomerDateUpdateDTO request = BulkCustomerDateUpdateDTO.builder()
                .promotionId(promotionId)
                .groupId(groupId)
                .customerIds(customerIds)
                .newStartDate(startDate)
                .newEndDate(endDate)
                .build();

        return updateDatesForCustomers(request, userId);
    }

    // Méthodes utilitaires privées

    private String calculatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) return "Date non définie";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (endDate == null) {
            return "Du " + startDate.format(formatter) + " → Illimitée";
        }

        long months = ChronoUnit.MONTHS.between(startDate, endDate);
        if (months < 12) {
            return "Du " + startDate.format(formatter) + " au " + endDate.format(formatter) +
                    " (" + months + " mois)";
        } else {
            long years = months / 12;
            long remainingMonths = months % 12;
            if (remainingMonths == 0) {
                return "Du " + startDate.format(formatter) + " au " + endDate.format(formatter) +
                        " (" + years + " an" + (years > 1 ? "s" : "") + ")";
            } else {
                return "Du " + startDate.format(formatter) + " au " + endDate.format(formatter) +
                        " (" + years + " an" + (years > 1 ? "s" : "") + " " + remainingMonths + " mois)";
            }
        }
    }

    private String determineStatus(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate != null && today.isBefore(startDate)) {
            return "PENDING";
        } else if (endDate != null && today.isAfter(endDate)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }

    private String formatPromotionValue(Promotion promotion) {
        String typeReduction = promotion.getTypeReduction();

        if (typeReduction != null &&
                typeReduction.equalsIgnoreCase("POURCENTAGE")) {

            return promotion.getValeurReduction() + "%";
        }

        return promotion.getValeurReduction() + " TND";
    }
}