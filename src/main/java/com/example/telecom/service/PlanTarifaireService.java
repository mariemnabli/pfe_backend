package com.example.telecom.service;

import com.example.telecom.dto.PlanTarifaireDTO;
import com.example.telecom.entity.PlanTarifaire;
import com.example.telecom.repository.PlanTarifaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<PlanTarifaireDTO> getAll() {
        return planTarifaireRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void supprimer(Long id) {
        planTarifaireRepository.deleteById(id);
    }

    private PlanTarifaireDTO toDTO(PlanTarifaire p) {
        return PlanTarifaireDTO.builder()
                .id(p.getId()).nom(p.getNom()).prixMensuel(p.getPrixMensuel()).description(p.getDescription()).build();
    }
}