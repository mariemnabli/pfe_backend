package com.example.telecom.controller;

import com.example.telecom.dto.AddServicesDTO;
import com.example.telecom.dto.OffreDTO;
import com.example.telecom.service.OffreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OffreController {

    private final OffreService offreService;

    // Créer une offre avec ses services en une seule requête
    @PostMapping
    public ResponseEntity<OffreDTO> creer(@RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.creer(dto));
    }

    // Modifier une offre (remplace tous les services)
    @PutMapping("/{id}")
    public ResponseEntity<OffreDTO> modifier(@PathVariable Long id, @RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.modifier(id, dto));
    }

    // Ajouter des services à une offre existante
    @PostMapping("/{id}/services")
    public ResponseEntity<OffreDTO> ajouterServices(
            @PathVariable Long id,
            @RequestBody AddServicesDTO dto) {
        return ResponseEntity.ok(offreService.ajouterServices(id, dto.getServiceIds()));
    }

    // Retirer un service d'une offre
    @DeleteMapping("/{offreId}/services/{serviceId}")
    public ResponseEntity<OffreDTO> retirerService(
            @PathVariable Long offreId,
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(offreService.retirerService(offreId, serviceId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OffreDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OffreDTO>> getAll() {
        return ResponseEntity.ok(offreService.getAll());
    }
}