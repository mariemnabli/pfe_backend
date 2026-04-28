package com.example.telecom.repository;

import com.example.telecom.entity.PromotionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromotionAssignmentRepository extends JpaRepository<PromotionAssignment, Long> {
    List<PromotionAssignment> findByPromotionId(Long promotionId);

    List<PromotionAssignment> findByTargetCustomerId(Long customerId);

    List<PromotionAssignment> findByTargetGroupId(Long groupId);

    List<PromotionAssignment> findByTargetContractId(Long contractId);
}
