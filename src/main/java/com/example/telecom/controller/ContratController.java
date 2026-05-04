package com.example.telecom.controller;

import com.example.telecom.dto.BulkImportResponse;
import com.example.telecom.dto.ContratDTO;
import com.example.telecom.service.ContratService;
import com.example.telecom.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContratController {

    private final ContratService contratService;
    private final CsvImportService csvImportService;

    @PostMapping
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ContratDTO> creer(@RequestBody ContratDTO dto) {
        return ResponseEntity.ok(contratService.creerContrat(dto));
    }

    @PostMapping("/upload-csv")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<BulkImportResponse<ContratDTO>> uploadCsv(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        List<ContratDTO> contrats = contratService.creerLots(csvImportService.parseContrats(file));
        return ResponseEntity.ok(BulkImportResponse.<ContratDTO>builder()
                .resourceType("contrats")
                .createdCount(contrats.size())
                .items(contrats)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ContratDTO> modifier(@PathVariable Long id, @RequestBody ContratDTO dto) {
        return ResponseEntity.ok(contratService.modifierContrat(id, dto));
    }

    @PutMapping("/{id}/resilier")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ContratDTO> resilier(@PathVariable Long id) {
        return ResponseEntity.ok(contratService.resilierContrat(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ContratDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contratService.getContrat(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<List<ContratDTO>> getAll() {
        return ResponseEntity.ok(contratService.getAllContrats());
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<List<ContratDTO>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contratService.getContratsByClient(clientId));
    }

    @GetMapping("/group/{customerGroupId}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<List<ContratDTO>> getByGroup(@PathVariable Long customerGroupId) {
        return ResponseEntity.ok(contratService.getContratsByGroup(customerGroupId));
    }

    // Ajouter une offre à un contrat existant
    @PutMapping("/{id}/add-offre/{offreId}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ContratDTO> addOffre(@PathVariable Long id, @PathVariable Long offreId) {
        return ResponseEntity.ok(contratService.ajouterOffreAuContrat(id, offreId));
    }
}
