package com.example.telecom.service;

import com.example.telecom.dto.ClientDTO;
import com.example.telecom.entity.Client;
import com.example.telecom.entity.CustomerGroup;
import com.example.telecom.entity.CustomerGroupMember;
import com.example.telecom.repository.ClientRepository;
import com.example.telecom.repository.CustomerGroupMemberRepository;
import com.example.telecom.repository.CustomerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final CustomerGroupMemberRepository customerGroupMemberRepository;

    // ── Créer un client ────────────────────────────────────────
    public ClientDTO creerClient(ClientDTO dto) {
        validerDocument(dto);

        Client client = Client.builder()
                .customerId(genererCustomerId())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .telephone(dto.getTelephone())
                .email(dto.getEmail())
                .adresse(dto.getAdresse())
                .ville(dto.getVille())
                .documentType(dto.getDocumentType())
                .build();

        // Remplir les champs selon le type de document
        if (dto.getDocumentType() == 1) {
            if (clientRepository.existsByCinNumber(dto.getCinNumber())) {
                throw new RuntimeException("Ce numéro CIN existe déjà : " + dto.getCinNumber());
            }
            client.setCinNumber(dto.getCinNumber());
            client.setCinImagePath(dto.getCinImagePath());
        } else {
            if (clientRepository.existsByPassportNumber(dto.getPassportNumber())) {
                throw new RuntimeException("Ce numéro de passport existe déjà : " + dto.getPassportNumber());
            }
            client.setPassportNumber(dto.getPassportNumber());
            client.setPassportImagePath(dto.getPassportImagePath());
        }

        Client saved = clientRepository.save(client);
        rattacherAuGroupeSiNecessaire(saved, dto.getCustomerGroupId());
        return toDTO(saved);
    }

    @Transactional
    public List<ClientDTO> creerClients(List<ClientDTO> dtos) {
        return dtos.stream()
                .map(this::creerClient)
                .collect(Collectors.toList());
    }

    // ── Modifier un client ─────────────────────────────────────
    public ClientDTO modifierClient(Long id, ClientDTO dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + id));

        validerDocument(dto);

        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setTelephone(dto.getTelephone());
        client.setEmail(dto.getEmail());
        client.setAdresse(dto.getAdresse());
        client.setVille(dto.getVille());
        client.setDocumentType(dto.getDocumentType());

        // Réinitialiser les champs de l'ancien document
        client.setCinNumber(null);
        client.setCinImagePath(null);
        client.setPassportNumber(null);
        client.setPassportImagePath(null);

        if (dto.getDocumentType() == 1) {
            verifierUniciteCin(dto.getCinNumber(), client.getId());
            client.setCinNumber(dto.getCinNumber());
            client.setCinImagePath(dto.getCinImagePath());
        } else {
            verifierUnicitePassport(dto.getPassportNumber(), client.getId());
            client.setPassportNumber(dto.getPassportNumber());
            client.setPassportImagePath(dto.getPassportImagePath());
        }

        Client saved = clientRepository.save(client);
        syncGroupMembership(saved, dto.getCustomerGroupId());
        return toDTO(saved);
    }

    // ── Getters ────────────────────────────────────────────────
    public ClientDTO getClient(Long id) {
        return toDTO(clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + id)));
    }

    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public void supprimerClient(Long id) {
        customerGroupMemberRepository.findByCustomerId(id).forEach(customerGroupMemberRepository::delete);
        clientRepository.deleteById(id);
    }

    // ── Générer customer_id format 000001 ──────────────────────
    private String genererCustomerId() {
        Long maxId = clientRepository.findMaxId().orElse(0L);
        long prochain = maxId + 1;
        return String.format("%06d", prochain);  // 000001, 000002 ...
    }

    // ── Validation document ────────────────────────────────────
    private void validerDocument(ClientDTO dto) {
        if (dto.getDocumentType() == null) {
            throw new RuntimeException("Le type de document est obligatoire (1=CIN, 2=PASSPORT)");
        }
        if (dto.getDocumentType() == 1) {
            if (dto.getCinNumber() == null || dto.getCinNumber().isBlank()) {
                throw new RuntimeException("Le numéro CIN est obligatoire pour documentType=1");
            }
        } else if (dto.getDocumentType() == 2) {
            if (dto.getPassportNumber() == null || dto.getPassportNumber().isBlank()) {
                throw new RuntimeException("Le numéro de passport est obligatoire pour documentType=2");
            }
        } else {
            throw new RuntimeException("documentType invalide. Valeurs acceptées : 1 (CIN) ou 2 (PASSPORT)");
        }
    }

    // ── Mapper entité → DTO ────────────────────────────────────
    private ClientDTO toDTO(Client c) {
        CustomerGroupMember activeMembership = customerGroupMemberRepository
                .findFirstByCustomerIdAndStatus(c.getId(), CustomerGroupMember.MembershipStatus.ACTIVE)
                .orElse(null);
        CustomerGroup group = activeMembership != null ? activeMembership.getCustomerGroup() : null;

        return ClientDTO.builder()
                .id(c.getId())
                .customerId(c.getCustomerId())
                .nom(c.getNom())
                .prenom(c.getPrenom())
                .telephone(c.getTelephone())
                .email(c.getEmail())
                .adresse(c.getAdresse())
                .ville(c.getVille())
                .documentType(c.getDocumentType())
                .cinNumber(c.getCinNumber())
                .cinImagePath(c.getCinImagePath())
                .passportNumber(c.getPassportNumber())
                .passportImagePath(c.getPassportImagePath())
                .customerGroupId(group != null ? group.getId() : null)
                .customerGroup(group != null ? ClientDTO.GroupSummary.builder()
                        .id(group.getId())
                        .groupCode(group.getGroupCode())
                        .name(group.getName())
                        .build() : null)
                .build();
    }

    private void verifierUniciteCin(String cinNumber, Long currentClientId) {
        clientRepository.findByCinNumber(cinNumber)
                .filter(client -> !client.getId().equals(currentClientId))
                .ifPresent(client -> {
                    throw new RuntimeException("Ce numéro CIN existe déjà : " + cinNumber);
                });
    }

    private void verifierUnicitePassport(String passportNumber, Long currentClientId) {
        clientRepository.findByPassportNumber(passportNumber)
                .filter(client -> !client.getId().equals(currentClientId))
                .ifPresent(client -> {
                    throw new RuntimeException("Ce numéro de passport existe déjà : " + passportNumber);
                });
    }

    private void rattacherAuGroupeSiNecessaire(Client client, Long customerGroupId) {
        if (customerGroupId == null) {
            return;
        }

        CustomerGroup group = customerGroupRepository.findById(customerGroupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable : " + customerGroupId));

        CustomerGroupMember membership = CustomerGroupMember.builder()
                .customerGroup(group)
                .customer(client)
                .memberRole(CustomerGroupMember.MemberRole.USER)
                .joinedAt(java.time.LocalDate.now())
                .status(CustomerGroupMember.MembershipStatus.ACTIVE)
                .build();
        customerGroupMemberRepository.save(membership);
    }

    private void syncGroupMembership(Client client, Long customerGroupId) {
        CustomerGroupMember activeMembership = customerGroupMemberRepository
                .findFirstByCustomerIdAndStatus(client.getId(), CustomerGroupMember.MembershipStatus.ACTIVE)
                .orElse(null);

        if (customerGroupId == null) {
            if (activeMembership != null) {
                activeMembership.setStatus(CustomerGroupMember.MembershipStatus.INACTIVE);
                activeMembership.setLeftAt(java.time.LocalDate.now());
                customerGroupMemberRepository.save(activeMembership);
            }
            return;
        }

        if (activeMembership != null && activeMembership.getCustomerGroup().getId().equals(customerGroupId)) {
            return;
        }

        if (activeMembership != null) {
            activeMembership.setStatus(CustomerGroupMember.MembershipStatus.INACTIVE);
            activeMembership.setLeftAt(java.time.LocalDate.now());
            customerGroupMemberRepository.save(activeMembership);
        }

        rattacherAuGroupeSiNecessaire(client, customerGroupId);
    }
}
