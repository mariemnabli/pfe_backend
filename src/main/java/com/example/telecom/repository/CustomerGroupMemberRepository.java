package com.example.telecom.repository;

import com.example.telecom.entity.CustomerGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerGroupMemberRepository extends JpaRepository<CustomerGroupMember, Long> {
    List<CustomerGroupMember> findByCustomerGroupId(Long customerGroupId);

    List<CustomerGroupMember> findByCustomerId(Long customerId);

    List<CustomerGroupMember> findByCustomerIdAndStatus(Long customerId, CustomerGroupMember.MembershipStatus status);

    Optional<CustomerGroupMember> findFirstByCustomerIdAndStatus(Long customerId, CustomerGroupMember.MembershipStatus status);

    long countByCustomerGroupIdAndStatus(Long customerGroupId, CustomerGroupMember.MembershipStatus status);
}
