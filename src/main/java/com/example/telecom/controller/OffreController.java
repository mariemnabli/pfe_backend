package com.example.telecom.controller;

import com.example.telecom.dto.AddServicesDTO;
import com.example.telecom.dto.BulkImportResponse;
import com.example.telecom.dto.OffreDTO;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.service.CsvImportService;
import com.example.telecom.service.OffreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OffreController {

    private final OffreService offreService;
    private final CsvImportService csvImportService;

    // Créer une offre avec ses services en une seule requête
    @PostMapping
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<OffreDTO> creer(@RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.creer(dto));
    }

    @PostMapping("/upload-csv")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<BulkImportResponse<OffreDTO>> uploadCsv(
            @RequestParam("file") MultipartFile file
    ) throws java.io.IOException {
        List<OffreDTO> offres = offreService.creerLots(csvImportService.parseOffres(file));
        return ResponseEntity.ok(BulkImportResponse.<OffreDTO>builder()
                .resourceType("offres")
                .createdCount(offres.size())
                .items(offres)
                .build());
    }

    // Modifier une offre (remplace tous les services)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<OffreDTO> modifier(@PathVariable Long id, @RequestBody OffreDTO dto) {
        return ResponseEntity.ok(offreService.modifier(id, dto));
    }

    // Supprimer une offre
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        offreService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // Ajouter des services à une offre existante
    @PostMapping("/{id}/services")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<OffreDTO> ajouterServices(
            @PathVariable Long id,
            @RequestBody AddServicesDTO dto) {
        return ResponseEntity.ok(offreService.ajouterServices(id, dto.getServiceIds()));
    }

    // Retirer un service d'une offre
    @DeleteMapping("/{offreId}/services/{serviceId}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<OffreDTO> retirerService(
            @PathVariable Long offreId,
            @PathVariable Long serviceId) {
        return ResponseEntity.ok(offreService.retirerService(offreId, serviceId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('METIER','VENTE','EXPLOIT')")
    public ResponseEntity<OffreDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<OffreDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(offreService.getAll(page, size));
    }
}
