package com.example.telecom.service;

import com.example.telecom.dto.ClientDTO;
import com.example.telecom.entity.Client;
import com.example.telecom.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

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

        return toDTO(clientRepository.save(client));
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
            client.setCinNumber(dto.getCinNumber());
            client.setCinImagePath(dto.getCinImagePath());
        } else {
            client.setPassportNumber(dto.getPassportNumber());
            client.setPassportImagePath(dto.getPassportImagePath());
        }

        return toDTO(clientRepository.save(client));
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
                .build();
    }
}