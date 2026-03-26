package com.example.telecom.service;

import com.example.telecom.dto.ReclamationDTO;
import com.example.telecom.entity.Client;
import com.example.telecom.entity.Reclamation;
import com.example.telecom.repository.ClientRepository;
import com.example.telecom.repository.ReclamationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final ClientRepository      clientRepository;

    public ReclamationDTO creer(ReclamationDTO dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));

        Reclamation r = Reclamation.builder()
                .description(dto.getDescription())
                .statut(Reclamation.StatutReclamation.OUVERTE)
                .commentaireVendeur(dto.getCommentaireVendeur())
                .client(client)
                .build();

        return toDTO(reclamationRepository.save(r));
    }

    public ReclamationDTO changerStatut(Long id, Reclamation.StatutReclamation statut) {
        Reclamation r = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable : " + id));
        r.setStatut(statut);
        return toDTO(reclamationRepository.save(r));
    }

    public ReclamationDTO modifier(Long id, ReclamationDTO dto) {
        Reclamation r = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable : " + id));
        if (dto.getDescription()        != null) r.setDescription(dto.getDescription());
        if (dto.getStatut()             != null) r.setStatut(dto.getStatut());
        if (dto.getCommentaireVendeur() != null) r.setCommentaireVendeur(dto.getCommentaireVendeur());
        if (dto.getClientId()           != null) {
            Client client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
            r.setClient(client);
        }
        return toDTO(reclamationRepository.save(r));
    }

    public void supprimer(Long id) { reclamationRepository.deleteById(id); }

    public ReclamationDTO getById(Long id) {
        return toDTO(reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable : " + id)));
    }

    public List<ReclamationDTO> getAll() {
        return reclamationRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<ReclamationDTO> getByClient(Long clientId) {
        return reclamationRepository.findByClientId(clientId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ReclamationDTO toDTO(Reclamation r) {
        return ReclamationDTO.builder()
                .id(r.getId())
                .description(r.getDescription())
                .statut(r.getStatut())
                .dateCreation(r.getDateCreation())
                .dateMiseAJour(r.getDateMiseAJour())
                .commentaireVendeur(r.getCommentaireVendeur())
                .clientId(r.getClient() != null ? r.getClient().getId() : null)
                .client(r.getClient() != null ? ReclamationDTO.ClientInfo.builder()
                        .id(r.getClient().getId())
                        .nom(r.getClient().getNom())
                        .prenom(r.getClient().getPrenom())
                        .email(r.getClient().getEmail())
                        .telephone(r.getClient().getTelephone())
                        .build() : null)
                .build();
    }
}