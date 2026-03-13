package com.example.telecom.controller;

import com.example.telecom.dto.PlanTarifaireDTO;
import com.example.telecom.service.PlanTarifaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans-tarifaires")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlanTarifaireController {

    private final PlanTarifaireService service;

    @PostMapping
    public ResponseEntity<PlanTarifaireDTO> creer(@RequestBody PlanTarifaireDTO dto) {
        return ResponseEntity.ok(service.creer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanTarifaireDTO> modifier(@PathVariable Long id, @RequestBody PlanTarifaireDTO dto) {
        return ResponseEntity.ok(service.modifier(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanTarifaireDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PlanTarifaireDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}