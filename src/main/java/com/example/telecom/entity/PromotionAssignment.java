package com.example.telecom.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

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
}
