package com.example.telecom.controller;

import com.example.telecom.dto.PlanTarifaireDTO;
import com.example.telecom.service.PlanTarifaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans-tarifaires")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanTarifaireController {

    private final PlanTarifaireService service;

    @PostMapping
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<PlanTarifaireDTO> creer(@RequestBody PlanTarifaireDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
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
    public ResponseEntity<List<PlanTarifaireDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('METIER')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
