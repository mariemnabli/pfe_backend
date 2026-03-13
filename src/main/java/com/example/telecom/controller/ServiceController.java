package com.example.telecom.controller;

import com.example.telecom.dto.ServiceDTO;
import com.example.telecom.service.ServiceTelecom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceTelecom serviceTelecom;

    @PostMapping
    public ResponseEntity<ServiceDTO> creer(@RequestBody ServiceDTO dto) {
        return ResponseEntity.ok(serviceTelecom.creer(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceDTO> modifier(@PathVariable Long id, @RequestBody ServiceDTO dto) {
        return ResponseEntity.ok(serviceTelecom.modifier(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceTelecom.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ServiceDTO>> getAll() {
        return ResponseEntity.ok(serviceTelecom.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        serviceTelecom.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}