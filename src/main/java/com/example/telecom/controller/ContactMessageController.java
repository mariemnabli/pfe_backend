package com.example.telecom.controller;

import com.example.telecom.dto.ContactMessageDTO;
import com.example.telecom.entity.ContactMessage;
import com.example.telecom.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContactMessageController {

    private final ContactMessageService contactMessageService;

    @PostMapping
    public ResponseEntity<ContactMessageDTO> creer(@RequestBody ContactMessageDTO dto) {
        return ResponseEntity.ok(contactMessageService.creer(dto));
    }

    @GetMapping
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<List<ContactMessageDTO>> getAll(
            @RequestParam(required = false) ContactMessage.StatutContact statut) {
        return ResponseEntity.ok(contactMessageService.getAll(statut));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<ContactMessageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contactMessageService.getById(id));
    }

    @PatchMapping("/{id}/reply")
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<ContactMessageDTO> repondre(@PathVariable Long id, @RequestBody ContactMessageDTO dto) {
        return ResponseEntity.ok(contactMessageService.repondre(id, dto));
    }
}
