package com.example.telecom.service;

import com.example.telecom.dto.ContratDTO;
import com.example.telecom.entity.*;
import com.example.telecom.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository contratRepository;
    private final ClientRepository clientRepository;
    private final OffreRepository offreRepository;

    public ContratDTO creerContrat(ContratDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
        Offre offre = offreRepository.findById(dto.getOffreId())
                .orElseThrow(() -> new RuntimeException("Offre introuvable : " + dto.getOffreId()));

        Contrat contrat = Contrat.builder()
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .statut(Contrat.StatutContrat.ACTIF)
                .client(client)
                .offre(offre)
                .build();

        return toDTO(contratRepository.save(contrat));
    }

    public ContratDTO modifierContrat(Long id, ContratDTO dto) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + id));
        contrat.setDateDebut(dto.getDateDebut());
        contrat.setDateFin(dto.getDateFin());
        if (dto.getStatut() != null) contrat.setStatut(dto.getStatut());
        return toDTO(contratRepository.save(contrat));
    }

    public ContratDTO resilierContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + id));
        contrat.setStatut(Contrat.StatutContrat.RESILIE);
        return toDTO(contratRepository.save(contrat));
    }

    public ContratDTO getContrat(Long id) {
        return toDTO(contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + id)));
    }

    public List<ContratDTO> getContratsByClient(Long clientId) {
        return contratRepository.findByClientId(clientId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ContratDTO> getAllContrats() {
        return contratRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ContratDTO toDTO(Contrat c) {
        return ContratDTO.builder()
                .id(c.getId())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .statut(c.getStatut())
                .clientId(c.getClient() != null ? c.getClient().getId() : null)
                .offreId(c.getOffre() != null ? c.getOffre().getId() : null)
                .build();
    }
}