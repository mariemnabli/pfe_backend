package com.example.telecom.service;

import com.example.telecom.dto.PromotionDTO;
import com.example.telecom.dto.PromotionAssignmentDTO;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.entity.Client;
import com.example.telecom.entity.Contrat;
import com.example.telecom.entity.CustomerGroup;
import com.example.telecom.entity.CustomerGroupMember;
import com.example.telecom.entity.Promotion;
import com.example.telecom.entity.PromotionAssignment;
import com.example.telecom.entity.Role;
import com.example.telecom.entity.User;
import com.example.telecom.repository.ClientRepository;
import com.example.telecom.repository.CustomerGroupMemberRepository;
import com.example.telecom.repository.ContratRepository;
import com.example.telecom.repository.CustomerGroupRepository;
import com.example.telecom.repository.PromotionAssignmentRepository;
import com.example.telecom.repository.PromotionRepository;
import com.example.telecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionAssignmentRepository promotionAssignmentRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final CustomerGroupMemberRepository customerGroupMemberRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final ContratRepository contratRepository;

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

    public PaginatedResponse<PromotionDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PromotionDTO> result = promotionRepository.findAll(pageable).map(this::toDTO);
        return buildPaginatedResponse(result);
    }

    public List<PromotionDTO> getByStatut(Promotion.StatutPromotion statut) {
        return promotionRepository.findByStatut(statut).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PromotionDTO getById(Long id) {
        return toDTO(getPromotion(id));
    }

    public PromotionAssignmentDTO assignerPromotion(Long promotionId, PromotionAssignmentDTO dto) {
        Promotion promotion = getPromotion(promotionId);
        User vendeur = resolveVendeur(dto.getAssignedById());

        if (dto.getTargetType() == null) {
            throw new RuntimeException("targetType est obligatoire");
        }

        PromotionAssignment assignment = PromotionAssignment.builder()
                .promotion(promotion)
                .targetType(dto.getTargetType())
                .status(PromotionAssignment.AssignmentStatus.SUSPENDED)
                .validationStatus(PromotionAssignment.ValidationStatus.PENDING)
                .assignmentMode(dto.getAssignmentMode() != null ? dto.getAssignmentMode() : PromotionAssignment.AssignmentMode.MANUAL)
                .effectiveStartDate(dto.getEffectiveStartDate() != null ? dto.getEffectiveStartDate() : LocalDate.now())
                .effectiveEndDate(dto.getEffectiveEndDate())
                .inheritedToMembers(dto.isInheritedToMembers())
                .assignedBy(vendeur)
                .build();

        switch (dto.getTargetType()) {
            case CUSTOMER -> assignment.setTargetCustomer(resolveCustomer(dto.getTargetCustomerId()));
            case CUSTOMER_GROUP -> assignment.setTargetGroup(resolveGroup(dto.getTargetGroupId()));
            case CONTRACT -> assignment.setTargetContract(resolveContract(dto.getTargetContractId()));
            default -> throw new RuntimeException("targetType non supporte");
        }

        return toAssignmentDTO(promotionAssignmentRepository.save(assignment));
    }

    public PromotionAssignmentDTO validerAssignment(Long promotionId, Long assignmentId, Long validateurId) {
        PromotionAssignment assignment = getAssignment(promotionId, assignmentId);
        User validateur = resolveExploit(validateurId);

        if (assignment.getValidationStatus() == PromotionAssignment.ValidationStatus.VALIDATED) {
            throw new RuntimeException("Cette affectation est deja validee");
        }

        assignment.setValidationStatus(PromotionAssignment.ValidationStatus.VALIDATED);
        assignment.setStatus(PromotionAssignment.AssignmentStatus.ACTIVE);
        assignment.setValidatedBy(validateur);
        assignment.setValidatedAt(LocalDateTime.now());
        return toAssignmentDTO(promotionAssignmentRepository.save(assignment));
    }

    public PromotionAssignmentDTO rejeterAssignment(Long promotionId, Long assignmentId, Long validateurId) {
        PromotionAssignment assignment = getAssignment(promotionId, assignmentId);
        User validateur = resolveExploit(validateurId);

        if (assignment.getValidationStatus() == PromotionAssignment.ValidationStatus.VALIDATED) {
            throw new RuntimeException("Impossible de rejeter une affectation deja validee");
        }

        assignment.setValidationStatus(PromotionAssignment.ValidationStatus.REJECTED);
        assignment.setValidatedBy(validateur);
        assignment.setValidatedAt(LocalDateTime.now());
        return toAssignmentDTO(promotionAssignmentRepository.save(assignment));
    }

    public List<PromotionAssignmentDTO> getAssignmentsByPromotion(Long promotionId) {
        getPromotion(promotionId);
        return promotionAssignmentRepository.findByPromotionId(promotionId).stream()
                .map(this::toAssignmentDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getPromotionsApplicablesAuClient(Long customerId) {
        resolveCustomer(customerId);
        List<Long> activeGroupIds = customerGroupMemberRepository
                .findByCustomerIdAndStatus(customerId, CustomerGroupMember.MembershipStatus.ACTIVE)
                .stream()
                .map(member -> member.getCustomerGroup().getId())
                .toList();

        return promotionRepository.findAll().stream()
                .filter(promotion -> hasActiveTarget(promotion, customerId, List.of(), activeGroupIds, null))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PromotionDTO> getPromotionsApplicablesAuGroupe(Long groupId) {
        resolveGroup(groupId);

        return promotionRepository.findAll().stream()
                .filter(promotion -> hasActiveTarget(promotion, null, List.of(groupId), List.of(), null))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public boolean isPromotionAssignableToContrat(Contrat contrat, Promotion promotion) {
        List<PromotionAssignment> assignments = promotionAssignmentRepository.findByPromotionId(promotion.getId());
        if (assignments.isEmpty()) {
            return true;
        }

        Long customerId = contrat.getClient() != null ? contrat.getClient().getId() : null;
        Long groupId = contrat.getCustomerGroup() != null ? contrat.getCustomerGroup().getId() : null;
        List<Long> inheritedGroupIds = customerId != null
                ? customerGroupMemberRepository.findByCustomerIdAndStatus(customerId, CustomerGroupMember.MembershipStatus.ACTIVE)
                  .stream()
                  .map(member -> member.getCustomerGroup().getId())
                  .toList()
                : List.of();

        Long contractId = contrat.getId();
        List<Long> directGroupIds = groupId != null ? List.of(groupId) : List.of();

        return assignments.stream().anyMatch(assignment -> assignmentMatches(assignment, customerId, directGroupIds, inheritedGroupIds, contractId));
    }

    private Promotion getPromotion(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion introuvable : " + id));
    }

    private PromotionAssignment getAssignment(Long promotionId, Long assignmentId) {
        getPromotion(promotionId);
        return promotionAssignmentRepository.findByIdAndPromotionId(assignmentId, promotionId)
                .orElseThrow(() -> new RuntimeException("Affectation introuvable : " + assignmentId));
    }

    // Métier: Modifier une promotion
    public PromotionDTO modifierPromotion(Long id, PromotionDTO dto) {
        Promotion promotion = getPromotion(id);

        // ❌ Bloquer modification si déjà active (option métier)
        if (promotion.getStatut() == Promotion.StatutPromotion.ACTIVE) {
            throw new RuntimeException("Impossible de modifier une promotion active");
        }

        if (promotion.getStatut() == Promotion.StatutPromotion.VALIDEE) {
            throw new RuntimeException("Modification interdite après validation");
        }

        if (dto.getDateFin().isBefore(dto.getDateDebut())) {
            throw new RuntimeException("La date de fin doit être après la date de début");
        }

        // ✅ Mise à jour des champs
        promotion.setNomPromotion(dto.getNomPromotion());
        promotion.setTypeReduction(dto.getTypeReduction());
        promotion.setValeurReduction(dto.getValeurReduction());
        promotion.setDateDebut(dto.getDateDebut());
        promotion.setDateFin(dto.getDateFin());
        promotion.setRegleEligibilite(dto.getRegleEligibilite());
        promotion.setAncienneteMinimale(dto.getAncienneteMinimale());

        // ⚠️ option métier : remettre en attente après modification
        promotion.setStatut(Promotion.StatutPromotion.EN_ATTENTE);

        return toDTO(promotionRepository.save(promotion));
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
                .createurId(p.getCreateur() != null ? p.getCreateur().getId() : null)
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
                .assignments(promotionAssignmentRepository.findByPromotionId(p.getId()).stream()
                        .map(this::toAssignmentDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private Client resolveCustomer(Long customerId) {
        if (customerId == null) {
            throw new RuntimeException("targetCustomerId est obligatoire");
        }
        return clientRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + customerId));
    }

    private CustomerGroup resolveGroup(Long groupId) {
        if (groupId == null) {
            throw new RuntimeException("targetGroupId est obligatoire");
        }
        return customerGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable : " + groupId));
    }

    private Contrat resolveContract(Long contractId) {
        if (contractId == null) {
            throw new RuntimeException("targetContractId est obligatoire");
        }
        return contratRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + contractId));
    }

    private PromotionAssignmentDTO toAssignmentDTO(PromotionAssignment assignment) {
        return PromotionAssignmentDTO.builder()
                .id(assignment.getId())
                .promotionId(assignment.getPromotion().getId())
                .targetType(assignment.getTargetType())
                .targetCustomerId(assignment.getTargetCustomer() != null ? assignment.getTargetCustomer().getId() : null)
                .targetGroupId(assignment.getTargetGroup() != null ? assignment.getTargetGroup().getId() : null)
                .targetContractId(assignment.getTargetContract() != null ? assignment.getTargetContract().getId() : null)
                .assignedById(assignment.getAssignedBy() != null ? assignment.getAssignedBy().getId() : null)
                .validatedById(assignment.getValidatedBy() != null ? assignment.getValidatedBy().getId() : null)
                .status(assignment.getStatus())
                .validationStatus(assignment.getValidationStatus())
                .assignmentMode(assignment.getAssignmentMode())
                .effectiveStartDate(assignment.getEffectiveStartDate())
                .effectiveEndDate(assignment.getEffectiveEndDate())
                .inheritedToMembers(assignment.isInheritedToMembers())
                .assignedAt(assignment.getAssignedAt())
                .validatedAt(assignment.getValidatedAt())
                .target(buildTargetSummary(assignment))
                .assignedBy(buildUserSummary(assignment.getAssignedBy()))
                .validatedBy(buildUserSummary(assignment.getValidatedBy()))
                .build();
    }

    private PaginatedResponse<PromotionDTO> buildPaginatedResponse(Page<PromotionDTO> page) {
        return PaginatedResponse.<PromotionDTO>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private PromotionAssignmentDTO.TargetSummary buildTargetSummary(PromotionAssignment assignment) {
        if (assignment.getTargetCustomer() != null) {
            return PromotionAssignmentDTO.TargetSummary.builder()
                    .type("CUSTOMER")
                    .id(assignment.getTargetCustomer().getId())
                    .label(assignment.getTargetCustomer().getNom() + " " + assignment.getTargetCustomer().getPrenom())
                    .build();
        }
        if (assignment.getTargetGroup() != null) {
            return PromotionAssignmentDTO.TargetSummary.builder()
                    .type("CUSTOMER_GROUP")
                    .id(assignment.getTargetGroup().getId())
                    .label(assignment.getTargetGroup().getName())
                    .build();
        }
        if (assignment.getTargetContract() != null) {
            return PromotionAssignmentDTO.TargetSummary.builder()
                    .type("CONTRACT")
                    .id(assignment.getTargetContract().getId())
                    .label(assignment.getTargetContract().getContractId())
                    .build();
        }
        return null;
    }

    private boolean hasActiveTarget(Promotion promotion, Long customerId, List<Long> directGroupIds, List<Long> inheritedGroupIds, Long contractId) {
        return promotionAssignmentRepository.findByPromotionId(promotion.getId()).stream()
                .anyMatch(assignment -> assignmentMatches(assignment, customerId, directGroupIds, inheritedGroupIds, contractId));
    }

    private boolean assignmentMatches(PromotionAssignment assignment, Long customerId, List<Long> directGroupIds, List<Long> inheritedGroupIds, Long contractId) {
        if (assignment.getStatus() != PromotionAssignment.AssignmentStatus.ACTIVE) {
            return false;
        }
        if (assignment.getValidationStatus() != PromotionAssignment.ValidationStatus.VALIDATED) {
            return false;
        }

        LocalDate today = LocalDate.now();
        if (assignment.getEffectiveStartDate() != null && today.isBefore(assignment.getEffectiveStartDate())) {
            return false;
        }
        if (assignment.getEffectiveEndDate() != null && today.isAfter(assignment.getEffectiveEndDate())) {
            return false;
        }

        return switch (assignment.getTargetType()) {
            case CUSTOMER -> assignment.getTargetCustomer() != null
                    && customerId != null
                    && assignment.getTargetCustomer().getId().equals(customerId);
            case CUSTOMER_GROUP -> matchesGroupAssignment(assignment, directGroupIds, inheritedGroupIds);
            case CONTRACT -> assignment.getTargetContract() != null
                    && contractId != null
                    && assignment.getTargetContract().getId().equals(contractId);
        };
    }

    private boolean matchesGroupAssignment(PromotionAssignment assignment, List<Long> directGroupIds, List<Long> inheritedGroupIds) {
        if (assignment.getTargetGroup() == null) {
            return false;
        }

        Long targetGroupId = assignment.getTargetGroup().getId();
        if (directGroupIds != null && directGroupIds.contains(targetGroupId)) {
            return true;
        }

        return inheritedGroupIds != null
                && inheritedGroupIds.contains(targetGroupId)
                && assignment.isInheritedToMembers();
    }

    private User resolveVendeur(Long vendeurId) {
        if (vendeurId == null) {
            throw new RuntimeException("assignedById est obligatoire");
        }
        User vendeur = userRepository.findById(vendeurId)
                .orElseThrow(() -> new RuntimeException("Vendeur introuvable : " + vendeurId));
        if (vendeur.getRole() != Role.VENTE) {
            throw new RuntimeException("assignedById doit referencer un utilisateur VENTE");
        }
        return vendeur;
    }

    private User resolveExploit(Long validateurId) {
        User validateur = userRepository.findById(validateurId)
                .orElseThrow(() -> new RuntimeException("Validateur introuvable : " + validateurId));
        if (validateur.getRole() != Role.EXPLOIT) {
            throw new RuntimeException("validateurId doit referencer un utilisateur EXPLOIT");
        }
        return validateur;
    }

    private PromotionAssignmentDTO.UserSummary buildUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return PromotionAssignmentDTO.UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }
}
