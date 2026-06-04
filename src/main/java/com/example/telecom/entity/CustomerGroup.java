package com.example.telecom.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "customer_groups",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name"),
                @UniqueConstraint(columnNames = "group_code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", unique = true, updatable = false)
    private String groupCode;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupType groupType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    @OneToMany(mappedBy = "customerGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<CustomerGroupMember> members = new ArrayList<>();

    public enum GroupType {
        ENTERPRISE,
        FAMILY,
        SME,
        OTHER
    }

    public enum GroupStatus {
        ACTIVE,
        INACTIVE
    }
}
