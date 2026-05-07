package com.example.telecom.service;

import com.example.telecom.dto.PlanTarifaireDTO;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.entity.PlanTarifaire;
import com.example.telecom.repository.PlanTarifaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanTarifaireService {

    private final PlanTarifaireRepository planTarifaireRepository;

    public PlanTarifaireDTO creer(PlanTarifaireDTO dto) {
        PlanTarifaire p = PlanTarifaire.builder()
                .nom(dto.getNom()).prixMensuel(dto.getPrixMensuel()).description(dto.getDescription()).build();
        return toDTO(planTarifaireRepository.save(p));
    }

    @Transactional
    public List<PlanTarifaireDTO> creerLots(List<PlanTarifaireDTO> dtos) {
        return dtos.stream()
                .map(this::creer)
                .collect(Collectors.toList());
    }

    public PlanTarifaireDTO modifier(Long id, PlanTarifaireDTO dto) {
        PlanTarifaire p = planTarifaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan tarifaire introuvable : " + id));
        p.setNom(dto.getNom());
        p.setPrixMensuel(dto.getPrixMensuel());
        p.setDescription(dto.getDescription());
        return toDTO(planTarifaireRepository.save(p));
    }

    public PlanTarifaireDTO getById(Long id) {
        return toDTO(planTarifaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan tarifaire introuvable : " + id)));
    }

    public PaginatedResponse<PlanTarifaireDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PlanTarifaireDTO> result = planTarifaireRepository.findAll(pageable).map(this::toDTO);
        return buildPaginatedResponse(result);
    }

    public void supprimer(Long id) {
        planTarifaireRepository.deleteById(id);
    }

    private PlanTarifaireDTO toDTO(PlanTarifaire p) {
        return PlanTarifaireDTO.builder()
                .id(p.getId()).nom(p.getNom()).prixMensuel(p.getPrixMensuel()).description(p.getDescription()).build();
    }

    private PaginatedResponse<PlanTarifaireDTO> buildPaginatedResponse(Page<PlanTarifaireDTO> page) {
        return PaginatedResponse.<PlanTarifaireDTO>builder()
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
