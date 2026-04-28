package com.example.telecom.service;

import com.example.telecom.dto.ContratDTO;
import com.example.telecom.entity.ContractHolderType;
import com.example.telecom.entity.ContractType;
import com.example.telecom.entity.Client;
import com.example.telecom.entity.Contrat;
import com.example.telecom.entity.CustomerGroup;
import com.example.telecom.entity.Offre;
import com.example.telecom.repository.ClientRepository;
import com.example.telecom.repository.ContratRepository;
import com.example.telecom.repository.CustomerGroupRepository;
import com.example.telecom.repository.OffreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContratService {

    private final ContratRepository contratRepository;
    private final ClientRepository clientRepository;
    private final CustomerGroupRepository customerGroupRepository;
    private final OffreRepository offreRepository;

    public ContratDTO creerContrat(ContratDTO dto) {
        HolderSelection holder = resolveHolder(dto);
        Offre offre = offreRepository.findById(dto.getOffreId())
                .orElseThrow(() -> new RuntimeException("Offre introuvable : " + dto.getOffreId()));

        Number directoryNumber = dto.getDirectoryNumber() != null
                ? dto.getDirectoryNumber()
                : genererDirectoryNumber();

        Contrat contrat = Contrat.builder()
                .contractId(genererContractId())
                .contractType(holder.contractType())
                .holderType(holder.holderType())
                .dateDebut(dto.getDateDebut())
                .dateFin(dto.getDateFin())
                .statut(Contrat.StatutContrat.ACTIF)
                .client(holder.client())
                .customerGroup(holder.customerGroup())
                .offre(offre)
                .directoryNumber(directoryNumber)
                .build();

        return toDTO(contratRepository.save(contrat));
    }

    // ✅ Génère un numéro tunisien aléatoire : 216 + 2 chiffres opérateur + 7 chiffres
    private Number genererDirectoryNumber() {
        // Préfixes opérateurs tunisiens : 20-29, 50-59, 90-99
        int[] prefixes = {20, 21, 22, 23, 25, 50, 52, 53, 55, 58, 90, 92, 94, 97, 98};
        Random random = new Random();
        int prefix = prefixes[random.nextInt(prefixes.length)];
        int suffix = 1000000 + random.nextInt(9000000); // 7 chiffres
        // Format : 216XXXXXXXXX (sans le +)
        return Long.parseLong("216" + prefix + suffix);
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

        if (dto.getClientId() != null || dto.getCustomerGroupId() != null || dto.getContractType() != null || dto.getHolderType() != null) {
            HolderSelection holder = resolveHolder(dto);
            contrat.setContractType(holder.contractType());
            contrat.setHolderType(holder.holderType());
            contrat.setClient(holder.client());
            contrat.setCustomerGroup(holder.customerGroup());
        }

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

    public List<ContratDTO> getContratsByGroup(Long customerGroupId) {
        return contratRepository.findByCustomerGroupId(customerGroupId).stream()
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

    private String genererContractId() {
        Long maxId = contratRepository.findMaxId().orElse(0L);
        long prochain = maxId + 1;
        return String.format("%06d", prochain);  // 000001, 000002 ...
    }

    private ContratDTO toDTO(Contrat c) {
        return ContratDTO.builder()
                .id(c.getId())
                .contractId(c.getContractId())
                .contractType(c.getContractType())
                .holderType(c.getHolderType())
                .dateDebut(c.getDateDebut())
                .dateFin(c.getDateFin())
                .statut(c.getStatut())
                .directoryNumber(c.getDirectoryNumber())
                .clientId(c.getClient() != null ? c.getClient().getId() : null)
                .customerGroupId(c.getCustomerGroup() != null ? c.getCustomerGroup().getId() : null)
                .offreId(c.getOffre()  != null ? c.getOffre().getId()  : null)
                .client(c.getClient() != null ? ContratDTO.ClientSummary.builder()
                        .id(c.getClient().getId())
                        .nom(c.getClient().getNom())
                        .prenom(c.getClient().getPrenom())
                        .email(c.getClient().getEmail())
                        .telephone(c.getClient().getTelephone())
                        .build() : null)
                .customerGroup(c.getCustomerGroup() != null ? ContratDTO.GroupSummary.builder()
                        .id(c.getCustomerGroup().getId())
                        .groupCode(c.getCustomerGroup().getGroupCode())
                        .name(c.getCustomerGroup().getName())
                        .groupType(c.getCustomerGroup().getGroupType().name())
                        .build() : null)
                .offre(c.getOffre() != null ? ContratDTO.OffreSummary.builder()
                        .id(c.getOffre().getId())
                        .nom(c.getOffre().getNomOffre())

                        .build() : null)
                .build();
    }

    private HolderSelection resolveHolder(ContratDTO dto) {
        ContractType contractType = dto.getContractType();
        ContractHolderType holderType = dto.getHolderType();

        if (contractType == null && dto.getCustomerGroupId() != null) {
            contractType = ContractType.ENTERPRISE;
        } else if (contractType == null) {
            contractType = ContractType.INDIVIDUAL;
        }

        if (holderType == null && contractType == ContractType.ENTERPRISE) {
            holderType = ContractHolderType.CUSTOMER_GROUP;
        } else if (holderType == null) {
            holderType = ContractHolderType.CUSTOMER;
        }

        if (contractType == ContractType.INDIVIDUAL && holderType != ContractHolderType.CUSTOMER) {
            throw new RuntimeException("Un contrat individuel doit etre porte par un client");
        }

        if (contractType == ContractType.ENTERPRISE && holderType != ContractHolderType.CUSTOMER_GROUP) {
            throw new RuntimeException("Un contrat entreprise doit etre porte par un groupe");
        }

        Client client = null;
        CustomerGroup customerGroup = null;

        if (holderType == ContractHolderType.CUSTOMER) {
            if (dto.getClientId() == null) {
                throw new RuntimeException("clientId est obligatoire pour un contrat individuel");
            }
            client = clientRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client introuvable : " + dto.getClientId()));
        } else {
            if (dto.getCustomerGroupId() == null) {
                throw new RuntimeException("customerGroupId est obligatoire pour un contrat entreprise");
            }
            customerGroup = customerGroupRepository.findById(dto.getCustomerGroupId())
                    .orElseThrow(() -> new RuntimeException("Groupe introuvable : " + dto.getCustomerGroupId()));
        }

        return new HolderSelection(contractType, holderType, client, customerGroup);
    }

    private record HolderSelection(
            ContractType contractType,
            ContractHolderType holderType,
            Client client,
            CustomerGroup customerGroup
    ) {}
}
