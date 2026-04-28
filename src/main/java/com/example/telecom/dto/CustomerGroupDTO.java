package com.example.telecom.dto;

import com.example.telecom.entity.CustomerGroup;
import com.example.telecom.entity.CustomerGroupMember;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerGroupDTO {
    private Long id;
    private String groupCode;
    private String name;
    private CustomerGroup.GroupType groupType;
    private CustomerGroup.GroupStatus status;
    private List<MemberDTO> members;
    private Long memberCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberDTO {
        private Long membershipId;
        private Long customerId;
        private String customerName;
        private String email;
        private CustomerGroupMember.MemberRole memberRole;
        private LocalDate joinedAt;
        private LocalDate leftAt;
        private boolean primaryMember;
        private CustomerGroupMember.MembershipStatus status;
    }
}
