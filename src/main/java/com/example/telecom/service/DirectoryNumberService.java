package com.example.telecom.service;

import com.example.telecom.dto.DirectoryNumberDTO;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.entity.Contrat;
import com.example.telecom.entity.DirectoryNumber;
import com.example.telecom.repository.ContratRepository;
import com.example.telecom.repository.DirectoryNumberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectoryNumberService {

    private final DirectoryNumberRepository directoryNumberRepository;
    private final ContratRepository contratRepository;

    public PaginatedResponse<DirectoryNumberDTO> getAll(DirectoryNumber.DirectoryNumberStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<DirectoryNumberDTO> result = status == null
                ? directoryNumberRepository.findAll(pageable).map(this::toDTO)
                : directoryNumberRepository.findAllByStatus(status, pageable).map(this::toDTO);
        return buildPaginatedResponse(result);
    }

    public DirectoryNumberDTO creer(DirectoryNumberDTO dto) {
        return creerDirectoryNumber(dto);
    }

    public List<DirectoryNumberDTO> importer(List<DirectoryNumberDTO> dtos) {
        return dtos.stream()
                .map(this::creerDirectoryNumber)
                .toList();
    }

    private DirectoryNumberDTO creerDirectoryNumber(DirectoryNumberDTO dto) {
        if (dto.getNumero() == null) {
            throw new RuntimeException("numero est obligatoire");
        }
        if (directoryNumberRepository.existsByNumero(dto.getNumero())) {
            throw new RuntimeException("Ce directory number existe deja : " + dto.getNumero());
        }

        Contrat contrat = resolveContrat(dto);
        DirectoryNumber.DirectoryNumberStatus status = resolveStatus(dto.getStatus(), contrat);

        DirectoryNumber directoryNumber = DirectoryNumber.builder()
                .numero(dto.getNumero())
                .status(status)
                .dateActivation(resolveDateActivation(dto, contrat, status))
                .dateDesactivation(resolveDateDesactivation(dto, contrat, status))
                .contrat(contrat)
                .build();

        return toDTO(directoryNumberRepository.save(directoryNumber));
    }

    private Contrat resolveContrat(DirectoryNumberDTO dto) {
        Contrat contratById = null;
        Contrat contratByCode = null;

        if (dto.getContratId() != null) {
            contratById = contratRepository.findById(dto.getContratId())
                    .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + dto.getContratId()));
        }

        if (dto.getContractId() != null && !dto.getContractId().isBlank()) {
            contratByCode = contratRepository.findByContractId(dto.getContractId())
                    .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + dto.getContractId()));
        }

        if (contratById != null && contratByCode != null && !contratById.getId().equals(contratByCode.getId())) {
            throw new RuntimeException("contratId et contractId referencent deux contrats differents");
        }

        return contratById != null ? contratById : contratByCode;
    }

    private DirectoryNumber.DirectoryNumberStatus resolveStatus(String rawStatus, Contrat contrat) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return contrat != null
                    ? DirectoryNumber.DirectoryNumberStatus.ACTIF
                    : DirectoryNumber.DirectoryNumberStatus.LIBRE;
        }

        DirectoryNumber.DirectoryNumberStatus status;
        try {
            status = DirectoryNumber.DirectoryNumberStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Status invalide pour directory number : " + rawStatus);
        }

        if (contrat == null && status != DirectoryNumber.DirectoryNumberStatus.LIBRE) {
            throw new RuntimeException("Un directory number avec status " + status + " doit reference un contrat");
        }

        return status;
    }

    private java.time.LocalDate resolveDateActivation(
            DirectoryNumberDTO dto,
            Contrat contrat,
            DirectoryNumber.DirectoryNumberStatus status
    ) {
        if (dto.getDateActivation() != null) {
            return dto.getDateActivation();
        }
        if (status == DirectoryNumber.DirectoryNumberStatus.ACTIF && contrat != null) {
            return contrat.getDateDebut() != null ? contrat.getDateDebut() : java.time.LocalDate.now();
        }
        return null;
    }

    private java.time.LocalDate resolveDateDesactivation(
            DirectoryNumberDTO dto,
            Contrat contrat,
            DirectoryNumber.DirectoryNumberStatus status
    ) {
        if (dto.getDateDesactivation() != null) {
            return dto.getDateDesactivation();
        }
        if (status == DirectoryNumber.DirectoryNumberStatus.DESACTIVE && contrat != null) {
            return contrat.getDateFin() != null ? contrat.getDateFin() : java.time.LocalDate.now();
        }
        return null;
    }

    private DirectoryNumberDTO toDTO(DirectoryNumber directoryNumber) {
        Contrat contrat = directoryNumber.getContrat();

        return DirectoryNumberDTO.builder()
                .id(directoryNumber.getId())
                .numero(directoryNumber.getNumero())
                .status(directoryNumber.getStatus().name())
                .dateActivation(directoryNumber.getDateActivation())
                .dateDesactivation(directoryNumber.getDateDesactivation())
                .contratId(contrat != null ? contrat.getId() : null)
                .contractId(contrat != null ? contrat.getContractId() : null)
                .clientId(contrat != null && contrat.getClient() != null ? contrat.getClient().getId() : null)
                .clientNom(contrat != null && contrat.getClient() != null ? contrat.getClient().getNom() : null)
                .clientPrenom(contrat != null && contrat.getClient() != null ? contrat.getClient().getPrenom() : null)
                .customerGroupId(contrat != null && contrat.getCustomerGroup() != null ? contrat.getCustomerGroup().getId() : null)
                .customerGroupName(contrat != null && contrat.getCustomerGroup() != null ? contrat.getCustomerGroup().getName() : null)
                .build();
    }

    private PaginatedResponse<DirectoryNumberDTO> buildPaginatedResponse(Page<DirectoryNumberDTO> page) {
        return PaginatedResponse.<DirectoryNumberDTO>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
