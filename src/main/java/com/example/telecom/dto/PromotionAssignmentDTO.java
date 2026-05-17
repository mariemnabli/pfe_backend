package com.example.telecom.dto;

import com.example.telecom.entity.PromotionAssignment;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionAssignmentDTO {
    private Long id;
    private Long promotionId;
    private PromotionAssignment.TargetType targetType;
    private Long targetCustomerId;
    private Long targetGroupId;
    private Long targetContractId;
    private Long assignedById;
    private Long validatedById;
    private PromotionAssignment.AssignmentStatus status;
    private PromotionAssignment.ValidationStatus validationStatus;
    private PromotionAssignment.AssignmentMode assignmentMode;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private boolean inheritedToMembers;
    private LocalDateTime assignedAt;
    private LocalDateTime validatedAt;
    private TargetSummary target;
    private UserSummary assignedBy;
    private UserSummary validatedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TargetSummary {
        private String type;
        private Long id;
        private String label;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private Long id;
        private String username;
        private String email;
        private String role;
    }
}
