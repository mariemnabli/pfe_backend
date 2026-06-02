package com.example.telecom.dto;

import com.example.telecom.entity.ContactMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDTO {
    private Long id;
    private String nom;
    private String email;
    private String sujet;
    private String message;
    private String reponseDsi;
    private ContactMessage.StatutContact statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private LocalDateTime dateReponse;
}
