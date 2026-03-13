package com.example.telecom.service;

import com.example.telecom.dto.OffreDTO;
import com.example.telecom.entity.Offre;
import com.example.telecom.entity.PlanTarifaire;
import com.example.telecom.entity.Services;
import com.example.telecom.repository.OffreRepository;
import com.example.telecom.repository.PlanTarifaireRepository;
import com.example.telecom.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OffreService {

    private final OffreRepository offreRepository;
    private final PlanTarifaireRepository planTarifaireRepository;
    private final ServiceRepository serviceRepository;

    public OffreDTO creer(OffreDTO dto) {
        Offre offre = buildOffre(new Offre(), dto);
        return toDTO(offreRepository.save(offre));
    }

    public OffreDTO modifier(Long id, OffreDTO dto) {
        Offre offre = offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre introuvable : " + id));
        buildOffre(offre, dto);
        return toDTO(offreRepository.save(offre));
    }

    public OffreDTO getById(Long id) {
        return toDTO(offreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre introuvable : " + id)));
    }

    public List<OffreDTO> getAll() {
        return offreRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void supprimer(Long id) {
        offreRepository.deleteById(id);
    }

    // ── Ajouter des services à une offre existante ─────────────
    public OffreDTO ajouterServices(Long offreId, List<Long> serviceIds) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable : " + offreId));


        List<Services> nouveauxServices = serviceRepository.findAllById(serviceIds);

        // Ajouter sans dupliquer
        for (Services s : nouveauxServices) {
            if (!offre.getServices().contains(s)) {
                offre.getServices().add(s);
            }
        }

        return toDTO(offreRepository.save(offre));
    }
    // ── Retirer un service d'une offre ─────────────────────────
    public OffreDTO retirerService(Long offreId, Long serviceId) {
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable"));

        offre.getServices().removeIf(s -> s.getId().equals(serviceId));
        return toDTO(offreRepository.save(offre));
    }

    // ── Méthode interne : construire l'offre depuis le DTO ─────
    private Offre buildOffre(Offre offre, OffreDTO dto) {
        offre.setNomOffre(dto.getNomOffre());
        offre.setTypeOffre(dto.getTypeOffre());

        if (dto.getPlanTarifaireId() != null) {
            PlanTarifaire plan = planTarifaireRepository.findById(dto.getPlanTarifaireId())
                    .orElseThrow(() -> new RuntimeException("Plan tarifaire introuvable"));
            offre.setPlanTarifaire(plan);
        }

        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()) {
            List<Services> services = serviceRepository.findAllById(dto.getServiceIds());
            if (services.size() != dto.getServiceIds().size()) {
                throw new RuntimeException("Un ou plusieurs services sont introuvables");
            }
            offre.setServices(services);
        }
        return offre;
    }

    // ── Mapper entité → DTO ────────────────────────────────────
    private OffreDTO toDTO(Offre o) {
        return OffreDTO.builder()
                .id(o.getId())
                .nomOffre(o.getNomOffre())
                .typeOffre(o.getTypeOffre())
                .planTarifaireId(o.getPlanTarifaire() != null ? o.getPlanTarifaire().getId() : null)
                .serviceIds(o.getServices() != null
                        ? o.getServices().stream().map(Services::getId).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}