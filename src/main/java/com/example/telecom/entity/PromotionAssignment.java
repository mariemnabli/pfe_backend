package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promotion_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_customer_id")
    private Client targetCustomer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_group_id")
    private CustomerGroup targetGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_contract_id")
    private Contrat targetContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User assignedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validated_by_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private User validatedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.SUSPENDED;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false)
    @Builder.Default
    private ValidationStatus validationStatus = ValidationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssignmentMode assignmentMode = AssignmentMode.MANUAL;

    @Column(nullable = false)
    private LocalDate effectiveStartDate;

    private LocalDate effectiveEndDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean inheritedToMembers = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime validatedAt;

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public enum TargetType {
        CUSTOMER,
        CUSTOMER_GROUP,
        CONTRACT
    }

    public enum AssignmentStatus {
        ACTIVE,
        SUSPENDED,
        REMOVED,
        EXPIRED
    }

    public enum AssignmentMode {
        MANUAL,
        AUTOMATIC,
        MIGRATED
    }

    public enum ValidationStatus {
        PENDING,
        VALIDATED,
        REJECTED
    }
}
