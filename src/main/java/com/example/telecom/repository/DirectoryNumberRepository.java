package com.example.telecom.repository;

import com.example.telecom.entity.DirectoryNumber;
import com.example.telecom.entity.DirectoryNumber.DirectoryNumberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DirectoryNumberRepository extends JpaRepository<DirectoryNumber, Long> {
    Optional<DirectoryNumber> findByNumero(Long numero);
    List<DirectoryNumber> findByContratIdOrderByIdDesc(Long contratId);
    Optional<DirectoryNumber> findFirstByContratIdAndStatusOrderByIdDesc(Long contratId, DirectoryNumberStatus status);
    Page<DirectoryNumber> findAllByStatus(DirectoryNumberStatus status, Pageable pageable);
    boolean existsByNumero(Long numero);
}
