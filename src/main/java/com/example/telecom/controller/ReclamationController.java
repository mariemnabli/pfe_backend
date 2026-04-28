package com.example.telecom.controller;

import com.example.telecom.dto.ReclamationDTO;
import com.example.telecom.entity.Reclamation;
import com.example.telecom.service.ReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReclamationController {

    private final ReclamationService reclamationService;

    @PostMapping
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ReclamationDTO> creer(@RequestBody ReclamationDTO dto) {
        return ResponseEntity.ok(reclamationService.creer(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<ReclamationDTO> modifier(@PathVariable Long id, @RequestBody ReclamationDTO dto) {
        return ResponseEntity.ok(reclamationService.modifier(id, dto));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<ReclamationDTO> changerStatut(
            @PathVariable Long id,
            @RequestParam Reclamation.StatutReclamation statut) {
        return ResponseEntity.ok(reclamationService.changerStatut(id, statut));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<List<ReclamationDTO>> getAll() {
        return ResponseEntity.ok(reclamationService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<ReclamationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reclamationService.getById(id));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<List<ReclamationDTO>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(reclamationService.getByClient(clientId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTE','EXPLOIT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        reclamationService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
