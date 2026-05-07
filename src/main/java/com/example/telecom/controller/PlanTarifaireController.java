package com.example.telecom.controller;

import com.example.telecom.dto.BulkImportResponse;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.dto.PlanTarifaireDTO;
import com.example.telecom.service.CsvImportService;
import com.example.telecom.service.PlanTarifaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/plans-tarifaires")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanTarifaireController {

    private final PlanTarifaireService service;
    private final CsvImportService csvImportService;

    @PostMapping
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<PlanTarifaireDTO> creer(@RequestBody PlanTarifaireDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    @PostMapping("/upload-csv")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<BulkImportResponse<PlanTarifaireDTO>> uploadCsv(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        List<PlanTarifaireDTO> plans = service.creerLots(csvImportService.parsePlansTarifaires(file));
        return ResponseEntity.ok(BulkImportResponse.<PlanTarifaireDTO>builder()
                .resourceType("plans-tarifaires")
                .createdCount(plans.size())
                .items(plans)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<PlanTarifaireDTO> modifier(@PathVariable Long id, @RequestBody PlanTarifaireDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('METIER','VENTE','EXPLOIT')")
    public ResponseEntity<PlanTarifaireDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('METIER','VENTE','EXPLOIT')")
    public ResponseEntity<PaginatedResponse<PlanTarifaireDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
