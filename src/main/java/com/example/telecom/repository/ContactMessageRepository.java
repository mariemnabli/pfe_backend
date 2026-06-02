package com.example.telecom.repository;

import com.example.telecom.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    List<ContactMessage> findAllByOrderByDateCreationDesc();
    List<ContactMessage> findByStatutOrderByDateCreationDesc(ContactMessage.StatutContact statut);
}
