package com.example.telecom.controller;

import com.example.telecom.dto.ContratDTO;
import com.example.telecom.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContratController {

    private final ContratService contratService;

    @PostMapping
    public ResponseEntity<ContratDTO> creer(@RequestBody ContratDTO dto) {
        return ResponseEntity.ok(contratService.creerContrat(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContratDTO> modifier(@PathVariable Long id, @RequestBody ContratDTO dto) {
        return ResponseEntity.ok(contratService.modifierContrat(id, dto));
    }

    @PutMapping("/{id}/resilier")
    public ResponseEntity<ContratDTO> resilier(@PathVariable Long id) {
        return ResponseEntity.ok(contratService.resilierContrat(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContratDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(contratService.getContrat(id));
    }

    @GetMapping
    public ResponseEntity<List<ContratDTO>> getAll() {
        return ResponseEntity.ok(contratService.getAllContrats());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ContratDTO>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(contratService.getContratsByClient(clientId));
    }
    // Ajouter une offre à un contrat existant
    @PutMapping("/{id}/add-offre/{offreId}")
    public ResponseEntity<ContratDTO> addOffre(@PathVariable Long id, @PathVariable Long offreId) {
        return ResponseEntity.ok(contratService.ajouterOffreAuContrat(id, offreId));
    }
}