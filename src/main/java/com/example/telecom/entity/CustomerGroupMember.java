package com.example.telecom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "customer_group_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_group_id", nullable = false)
    @JsonIgnore
    private CustomerGroup customerGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Client customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberRole memberRole = MemberRole.USER;

    @Column(nullable = false)
    private LocalDate joinedAt;

    private LocalDate leftAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean primaryMember = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MembershipStatus status = MembershipStatus.ACTIVE;

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE && leftAt == null;
    }

    public enum MemberRole {
        OWNER,
        BILLING,
        USER,
        DECISION_MAKER
    }

    public enum MembershipStatus {
        ACTIVE,
        INACTIVE
    }
}
