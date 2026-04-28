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
    private PromotionAssignment.AssignmentStatus status;
    private PromotionAssignment.AssignmentMode assignmentMode;
    private LocalDate effectiveStartDate;
    private LocalDate effectiveEndDate;
    private boolean inheritedToMembers;
    private LocalDateTime assignedAt;
    private TargetSummary target;

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
}
