package com.example.telecom.scheduler;

import com.example.telecom.entity.PromotionAssignment;
import com.example.telecom.entity.Role;
import com.example.telecom.repository.PromotionAssignmentRepository;
import com.example.telecom.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionAssignmentExpirationScheduler {

    private final PromotionAssignmentRepository promotionAssignmentRepository;
    private final NotificationService notificationService;

    /**
     * Tourne chaque jour à minuit.
     * Expire tous les assignments actifs dont effectiveEndDate < aujourd'hui.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireAssignmentsEchus() {
        LocalDate today = LocalDate.now();
        log.info("[CRON] Vérification des assignments expirés — date : {}", today);

        List<PromotionAssignment> expiredAssignments = promotionAssignmentRepository
                .findByStatusAndEffectiveEndDateBefore(
                        PromotionAssignment.AssignmentStatus.ACTIVE,
                        today
                );

        if (expiredAssignments.isEmpty()) {
            log.info("[CRON] Aucun assignment expiré trouvé.");
            return;
        }

        for (PromotionAssignment assignment : expiredAssignments) {
            assignment.setStatus(PromotionAssignment.AssignmentStatus.EXPIRED);
            promotionAssignmentRepository.save(assignment);

            log.info("[CRON] Assignment #{} expiré — promotion : \"{}\"",
                    assignment.getId(),
                    assignment.getPromotion().getNomPromotion());

            // Notifier le vendeur qui avait fait l'affectation
            if (assignment.getAssignedBy() != null) {
                notificationService.notifyUser(
                        assignment.getAssignedBy(),
                        "ASSIGNMENT_EXPIRE",
                        "Affectation expirée",
                        "L'affectation de la promotion \""
                                + assignment.getPromotion().getNomPromotion()
                                + "\" a expiré le " + assignment.getEffectiveEndDate() + ".",
                        "PROMOTION_ASSIGNMENT",
                        assignment.getId()
                );
            }

            // Notifier aussi l'équipe EXPLOIT
            notificationService.notifyRole(
                    Role.EXPLOIT,
                    "ASSIGNMENT_EXPIRE",
                    "Affectation expirée",
                    "L'affectation #" + assignment.getId()
                            + " de la promotion \""
                            + assignment.getPromotion().getNomPromotion()
                            + "\" a expiré.",
                    "PROMOTION_ASSIGNMENT",
                    assignment.getId()
            );
        }

        log.info("[CRON] {} assignment(s) expiré(s) avec succès.", expiredAssignments.size());
    }
}