package com.example.telecom.service;

import com.example.telecom.dto.ContactMessageDTO;
import com.example.telecom.entity.ContactMessage;
import com.example.telecom.entity.Role;
import com.example.telecom.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public ContactMessageDTO creer(ContactMessageDTO dto) {
        validateCreationRequest(dto);

        ContactMessage saved = contactMessageRepository.save(ContactMessage.builder()
                .nom(dto.getNom().trim())
                .email(dto.getEmail().trim())
                .sujet(dto.getSujet().trim())
                .message(dto.getMessage().trim())
                .statut(ContactMessage.StatutContact.NOUVEAU)
                .build());

        notificationService.notifyRole(
                Role.DSI,
                "CONTACT_CREE",
                "Nouveau message de contact",
                "Nouveau message de contact de " + saved.getNom() + " : " + saved.getSujet(),
                "CONTACT",
                saved.getId()
        );

        return toDTO(saved);
    }

    public List<ContactMessageDTO> getAll(ContactMessage.StatutContact statut) {
        List<ContactMessage> contacts = statut == null
                ? contactMessageRepository.findAllByOrderByDateCreationDesc()
                : contactMessageRepository.findByStatutOrderByDateCreationDesc(statut);

        return contacts.stream().map(this::toDTO).toList();
    }

    public ContactMessageDTO getById(Long id) {
        return toDTO(findByIdOrThrow(id));
    }

    public ContactMessageDTO repondre(Long id, ContactMessageDTO dto) {
        ContactMessage contact = findByIdOrThrow(id);

        if (dto.getReponseDsi() == null || dto.getReponseDsi().isBlank()) {
            throw new RuntimeException("La reponse DSI est obligatoire");
        }

        contact.setReponseDsi(dto.getReponseDsi().trim());
        contact.setStatut(ContactMessage.StatutContact.REPONDU);
        contact.setDateReponse(LocalDateTime.now());

        ContactMessage saved = contactMessageRepository.save(contact);

        emailService.envoyerReponseContact(
                saved.getEmail(),
                saved.getNom(),
                saved.getSujet(),
                saved.getMessage(),
                saved.getReponseDsi()
        );

        return toDTO(saved);
    }

    private ContactMessage findByIdOrThrow(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message de contact introuvable : " + id));
    }

    private void validateCreationRequest(ContactMessageDTO dto) {
        if (dto.getNom() == null || dto.getNom().isBlank()) {
            throw new RuntimeException("Le nom est obligatoire");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("L'email est obligatoire");
        }
        if (!dto.getEmail().contains("@")) {
            throw new RuntimeException("L'email est invalide");
        }
        if (dto.getSujet() == null || dto.getSujet().isBlank()) {
            throw new RuntimeException("Le sujet est obligatoire");
        }
        if (dto.getMessage() == null || dto.getMessage().isBlank()) {
            throw new RuntimeException("Le message est obligatoire");
        }
    }

    private ContactMessageDTO toDTO(ContactMessage contact) {
        return ContactMessageDTO.builder()
                .id(contact.getId())
                .nom(contact.getNom())
                .email(contact.getEmail())
                .sujet(contact.getSujet())
                .message(contact.getMessage())
                .reponseDsi(contact.getReponseDsi())
                .statut(contact.getStatut())
                .dateCreation(contact.getDateCreation())
                .dateMiseAJour(contact.getDateMiseAJour())
                .dateReponse(contact.getDateReponse())
                .build();
    }
}
