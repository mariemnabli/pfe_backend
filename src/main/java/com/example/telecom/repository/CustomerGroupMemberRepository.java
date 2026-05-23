package com.example.telecom.repository;

import com.example.telecom.entity.CustomerGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerGroupMemberRepository extends JpaRepository<CustomerGroupMember, Long> {
    List<CustomerGroupMember> findByCustomerGroupId(Long customerGroupId);

    List<CustomerGroupMember> findByCustomerId(Long customerId);

    List<CustomerGroupMember> findByCustomerIdAndStatus(Long customerId, CustomerGroupMember.MembershipStatus status);

    List<CustomerGroupMember> findByCustomerGroupIdAndStatus(Long groupId, CustomerGroupMember.MembershipStatus status);
    
    @Query("SELECT cgm FROM CustomerGroupMember cgm WHERE cgm.customerGroup.id = :groupId AND cgm.customer.id IN :customerIds")
    List<CustomerGroupMember> findByCustomerGroupIdAndCustomerIdIn(@Param("groupId") Long groupId,
                                                                   @Param("customerIds") List<Long> customerIds);


    Optional<CustomerGroupMember> findFirstByCustomerIdAndStatus(Long customerId, CustomerGroupMember.MembershipStatus status);

    long countByCustomerGroupIdAndStatus(Long customerGroupId, CustomerGroupMember.MembershipStatus status);
}
