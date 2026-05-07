package com.example.telecom.controller;

import com.example.telecom.dto.BulkImportResponse;
import com.example.telecom.dto.PaginatedResponse;
import com.example.telecom.dto.ServiceDTO;
import com.example.telecom.service.CsvImportService;
import com.example.telecom.service.ServiceTelecom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceTelecom serviceTelecom;
    private final CsvImportService csvImportService;

    @PostMapping
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<ServiceDTO> creer(@RequestBody ServiceDTO dto) {
        return ResponseEntity.ok(serviceTelecom.creer(dto));
    }

    @PostMapping("/upload-csv")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<BulkImportResponse<ServiceDTO>> uploadCsv(
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        List<ServiceDTO> services = serviceTelecom.creerLots(csvImportService.parseServices(file));
        return ResponseEntity.ok(BulkImportResponse.<ServiceDTO>builder()
                .resourceType("services")
                .createdCount(services.size())
                .items(services)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<ServiceDTO> modifier(@PathVariable Long id, @RequestBody ServiceDTO dto) {
        return ResponseEntity.ok(serviceTelecom.modifier(id, dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('METIER','VENTE','EXPLOIT')")
    public ResponseEntity<ServiceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceTelecom.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('METIER','VENTE','EXPLOIT')")
    public ResponseEntity<PaginatedResponse<ServiceDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(serviceTelecom.getAll(page, size));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        serviceTelecom.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
