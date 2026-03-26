package com.example.telecom.service;

import com.example.telecom.dto.ContratDTO;
import com.example.telecom.entity.Client;
import com.example.telecom.entity.Contrat;
import com.example.telecom.entity.Offre;
import com.example.telecom.repository.ClientRepository;
import com.example.telecom.repository.ContratRepository;
import com.example.telecom.repository.OffreRepository;
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

    // -------------------- Création --------------------
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
                .directoryNumber(dto.getDirectoryNumber())
                .build();

        return toDTO(contratRepository.save(contrat));
    }

    // -------------------- Modification --------------------
    public ContratDTO modifierContrat(Long id, ContratDTO dto) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + id));

        // Dates
        if (dto.getDateDebut() != null) contrat.setDateDebut(dto.getDateDebut());
        if (dto.getDateFin()   != null) contrat.setDateFin(dto.getDateFin());
        if (dto.getStatut()    != null) contrat.setStatut(dto.getStatut());

        // ✅ Directory number
        if (dto.getDirectoryNumber() != null)
            contrat.setDirectoryNumber(dto.getDirectoryNumber());

        // ✅ Changer le client
        if (dto.getClientId() != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
            contrat.setClient(client);
        }

        // ✅ Changer l'offre
        if (dto.getOffreId() != null) {
            Offre offre = offreRepository.findById(dto.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre introuvable : " + dto.getOffreId()));
            contrat.setOffre(offre);
        }

        return toDTO(contratRepository.save(contrat));
    }

    // -------------------- Résiliation --------------------
    public ContratDTO resilierContrat(Long id) {
        Contrat contrat = contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + id));
        contrat.setStatut(Contrat.StatutContrat.RESILIE);
        return toDTO(contratRepository.save(contrat));
    }

    // -------------------- Récupération --------------------
    public ContratDTO getContrat(Long id) {
        return toDTO(contratRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + id)));
    }

    public List<ContratDTO> getContratsByClient(Long clientId) {
        return contratRepository.findByClientId(clientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ContratDTO> getAllContrats() {
        return contratRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // -------------------- Ajouter une offre --------------------
    public ContratDTO ajouterOffreAuContrat(Long contratId, Long offreId) {
        Contrat contrat = contratRepository.findById(contratId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable : " + contratId));
        Offre offre = offreRepository.findById(offreId)
                .orElseThrow(() -> new RuntimeException("Offre introuvable : " + offreId));

        // Si le contrat a un seul champ offre
        contrat.setOffre(offre);

        // Si le contrat peut avoir plusieurs offres (ManyToMany), tu ferais :
        // contrat.getOffres().add(offre);

        return toDTO(contratRepository.save(contrat));
    }

    // -------------------- Mapping DTO --------------------
    private ContratDTO toDTO(Contrat c) {
        return ContratDTO.builder()
                .id(c.getId())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .statut(c.getStatut())
                .directoryNumber(c.getDirectoryNumber())
                // IDs (compatibilité création)
                .clientId(c.getClient() != null ? c.getClient().getId() : null)
                .offreId(c.getOffre()  != null ? c.getOffre().getId()  : null)
                // Objets enrichis
                .client(c.getClient() != null ? ContratDTO.ClientSummary.builder()
                        .id(c.getClient().getId())
                        .nom(c.getClient().getNom())
                        .prenom(c.getClient().getPrenom())
                        .email(c.getClient().getEmail())
                        .telephone(c.getClient().getTelephone())
                        .build() : null)
                .offre(c.getOffre() != null ? ContratDTO.OffreSummary.builder()
                        .id(c.getOffre().getId())
                        .nom(c.getOffre().getNomOffre())

                        .build() : null)
                .build();
    }
}