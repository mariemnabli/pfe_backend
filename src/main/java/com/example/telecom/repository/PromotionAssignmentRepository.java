package com.example.telecom.repository;

import com.example.telecom.entity.PromotionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromotionAssignmentRepository extends JpaRepository<PromotionAssignment, Long> {
    List<PromotionAssignment> findByPromotionId(Long promotionId);

    java.util.Optional<PromotionAssignment> findByIdAndPromotionId(Long id, Long promotionId);

    List<PromotionAssignment> findByStatusAndEffectiveEndDateBefore(
            PromotionAssignment.AssignmentStatus status,
            LocalDate date
    );

    Optional<PromotionAssignment> findByPromotionIdAndTargetGroupId(Long promotionId, Long groupId);

    Optional<PromotionAssignment> findByPromotionIdAndTargetCustomerId(Long promotionId, Long customerId);

    @Query("SELECT pa FROM PromotionAssignment pa WHERE pa.promotion.id = :promotionId " +
            "AND pa.targetCustomer.id = :customerId AND pa.targetType = 'CUSTOMER_GROUP'")
    List<PromotionAssignment> findGroupAssignmentsByCustomer(@Param("promotionId") Long promotionId,
                                                             @Param("customerId") Long customerId);

    List<PromotionAssignment> findByTargetCustomerId(Long customerId);

    List<PromotionAssignment> findByTargetGroupId(Long groupId);

    List<PromotionAssignment> findAllByPromotionIdAndTargetCustomerId(Long promotionId, Long customerId);

    List<PromotionAssignment> findByTargetGroupIdAndPromotionId(Long groupId, Long promotionId);

    List<PromotionAssignment> findByTargetContractId(Long contractId);
}
