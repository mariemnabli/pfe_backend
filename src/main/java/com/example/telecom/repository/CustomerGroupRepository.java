package com.example.telecom.repository;

import com.example.telecom.entity.CustomerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerGroupRepository extends JpaRepository<CustomerGroup, Long> {
    Optional<CustomerGroup> findByGroupCode(String groupCode);

    @Query("SELECT MAX(g.id) FROM CustomerGroup g")
    Optional<Long> findMaxId();
}
