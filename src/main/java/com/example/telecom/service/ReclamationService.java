package com.example.telecom.service;

import com.example.telecom.dto.ReclamationDTO;
import com.example.telecom.entity.Client;
import com.example.telecom.entity.CustomerGroup;
import com.example.telecom.entity.CustomerGroupMember;
import com.example.telecom.entity.Reclamation;
import com.example.telecom.repository.ClientRepository;
import com.example.telecom.repository.CustomerGroupMemberRepository;
import com.example.telecom.repository.CustomerGroupRepository;
import com.example.telecom.repository.ReclamationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamationService {

    private final ReclamationRepository reclamationRepository;
    private final ClientRepository clientRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final CustomerGroupMemberRepository customerGroupMemberRepository;
    private final EmailService emailService;

    public ReclamationDTO creer(ReclamationDTO dto) {
        validateTarget(dto);

        Client client = null;
        CustomerGroup group = null;

        if (dto.getClientId() != null) {
            client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
        }

        if (dto.getCustomerGroupId() != null) {
            group = customerGroupRepository.findById(dto.getCustomerGroupId())
                    .orElseThrow(() -> new RuntimeException("Groupe introuvable : " + dto.getCustomerGroupId()));
        }

        Reclamation r = Reclamation.builder()
                .description(dto.getDescription())
                .statut(Reclamation.StatutReclamation.OUVERTE)
                .commentaireVendeur(dto.getCommentaireVendeur())
                .client(client)
                .customerGroup(group)
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

        if (dto.getDescription() != null) {
            r.setDescription(dto.getDescription());
        }

        if (dto.getCommentaireVendeur() != null) {
            r.setCommentaireVendeur(dto.getCommentaireVendeur());
        }

        return toDTO(reclamationRepository.save(r));
    }

    public ReclamationDTO repondre(Long id, ReclamationDTO dto) {
        Reclamation r = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable : " + id));
        String commentaireDsiAvant = r.getCommentaireDsi();
        if (dto.getStatut() != null) r.setStatut(dto.getStatut());
        if (dto.getCommentaireDsi() != null) r.setCommentaireDsi(dto.getCommentaireDsi());
        Reclamation saved = reclamationRepository.save(r);

        if (dto.getCommentaireDsi() != null
                && !dto.getCommentaireDsi().isBlank()
                && !dto.getCommentaireDsi().equals(commentaireDsiAvant)) {
            notifierClientReponseDsi(saved);
        }

        return toDTO(saved);
    }

    public void supprimer(Long id) {
        reclamationRepository.deleteById(id);
    }

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

    public List<ReclamationDTO> getByGroup(Long customerGroupId) {
        return reclamationRepository.findByCustomerGroupId(customerGroupId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    private ReclamationDTO toDTO(Reclamation r) {
        return ReclamationDTO.builder()
                .id(r.getId())
                .description(r.getDescription())
                .statut(r.getStatut())
                .dateCreation(r.getDateCreation())
                .dateMiseAJour(r.getDateMiseAJour())
                .commentaireVendeur(r.getCommentaireVendeur())
                .commentaireDsi(r.getCommentaireDsi())
                .clientId(r.getClient() != null ? r.getClient().getId() : null)
                .customerGroupId(r.getCustomerGroup() != null ? r.getCustomerGroup().getId() : null)
                .client(r.getClient() != null ? ReclamationDTO.ClientInfo.builder()
                                                .id(r.getClient().getId())
                                                .nom(r.getClient().getNom())
                                                .prenom(r.getClient().getPrenom())
                                                .email(r.getClient().getEmail())
                                                .telephone(r.getClient().getTelephone())
                                                .build() : null)
                .customerGroup(r.getCustomerGroup() != null ? ReclamationDTO.CustomerGroupInfo.builder()
                        .id(r.getCustomerGroup().getId())
                        .groupCode(r.getCustomerGroup().getGroupCode())
                        .name(r.getCustomerGroup().getName())
                        .groupType(r.getCustomerGroup().getGroupType() != null ? r.getCustomerGroup().getGroupType().name() : null)
                        .status(r.getCustomerGroup().getStatus() != null ? r.getCustomerGroup().getStatus().name() : null)
                        .build() : null)
                .build();
    }

    private void notifierClientReponseDsi(Reclamation reclamation) {
        Client client = reclamation.getClient();
        if (client != null) {
            notifierClient(client, reclamation);
            return;
        }

        CustomerGroup group = reclamation.getCustomerGroup();
        if (group == null) {
            return;
        }

        customerGroupMemberRepository.findByCustomerGroupId(group.getId()).stream()
                .filter(CustomerGroupMember::isActive)
                .map(CustomerGroupMember::getCustomer)
                .filter(Objects::nonNull)
                .filter(customer -> customer.getEmail() != null && !customer.getEmail().isBlank())
                .forEach(customer -> notifierClient(customer, reclamation));
    }

    private void notifierClient(Client client, Reclamation reclamation) {
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            return;
        }

        String nomClient = ((client.getPrenom() != null ? client.getPrenom() : "") + " "
                + (client.getNom() != null ? client.getNom() : "")).trim();

        emailService.envoyerReponseReclamation(
                client.getEmail(),
                nomClient.isBlank() ? "Client" : nomClient,
                reclamation.getDescription(),
                reclamation.getCommentaireDsi(),
                reclamation.getStatut() != null ? reclamation.getStatut().name() : null
        );
    }

    private void validateTarget(ReclamationDTO dto) {
        boolean hasClient = dto.getClientId() != null;
        boolean hasGroup = dto.getCustomerGroupId() != null;

        if (!hasClient && !hasGroup) {
            throw new RuntimeException("Il faut renseigner soit clientId soit customerGroupId");
        }

        if (hasClient && hasGroup) {
            throw new RuntimeException("Il faut renseigner soit clientId soit customerGroupId, pas les deux");
        }
    }
}
