package com.example.telecom.controller;

import com.example.telecom.dto.ClientDTO;
import com.example.telecom.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClientController {

    private final ClientService clientService;

    private static final String UPLOAD_DIR = "uploads/documents/";

    // ── Créer client (multipart : champs séparés + image) ──────
    // Postman : Body → form-data
    // Champs texte : nom, prenom, telephone, email, adresse, ville,
    //                documentType, cinNumber OU passportNumber
    // Champ fichier : image
    @PostMapping
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ClientDTO> creer(
            @RequestParam("nom")          String nom,
            @RequestParam("prenom")       String prenom,
            @RequestParam("telephone")    String telephone,
            @RequestParam("email")        String email,
            @RequestParam("adresse")      String adresse,
            @RequestParam("ville")        String ville,
            @RequestParam("documentType") Integer documentType,
            @RequestParam(value = "cinNumber",       required = false) String cinNumber,
            @RequestParam(value = "passportNumber",  required = false) String passportNumber,
            @RequestParam(value = "image",           required = false) MultipartFile image
    ) throws IOException {

        ClientDTO dto = new ClientDTO();
        dto.setNom(nom);
        dto.setPrenom(prenom);
        dto.setTelephone(telephone);
        dto.setEmail(email);
        dto.setAdresse(adresse);
        dto.setVille(ville);
        dto.setDocumentType(documentType);

        if (documentType == 1) {
            dto.setCinNumber(cinNumber);
            if (image != null && !image.isEmpty()) {
                dto.setCinImagePath(sauvegarderImage(image));
            }
        } else if (documentType == 2) {
            dto.setPassportNumber(passportNumber);
            if (image != null && !image.isEmpty()) {
                dto.setPassportImagePath(sauvegarderImage(image));
            }
        }

        return ResponseEntity.ok(clientService.creerClient(dto));
    }

    // ── Modifier client ────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ClientDTO> modifier(
            @PathVariable            Long id,
            @RequestParam("nom")          String nom,
            @RequestParam("prenom")       String prenom,
            @RequestParam("telephone")    String telephone,
            @RequestParam("email")        String email,
            @RequestParam("adresse")      String adresse,
            @RequestParam("ville")        String ville,
            @RequestParam("documentType") Integer documentType,
            @RequestParam(value = "cinNumber",       required = false) String cinNumber,
            @RequestParam(value = "passportNumber",  required = false) String passportNumber,
            @RequestParam(value = "image",           required = false) MultipartFile image
    ) throws IOException {

        ClientDTO dto = new ClientDTO();
        dto.setNom(nom);
        dto.setPrenom(prenom);
        dto.setTelephone(telephone);
        dto.setEmail(email);
        dto.setAdresse(adresse);
        dto.setVille(ville);
        dto.setDocumentType(documentType);

        if (documentType == 1) {
            dto.setCinNumber(cinNumber);
            if (image != null && !image.isEmpty()) {
                dto.setCinImagePath(sauvegarderImage(image));
            }
        } else if (documentType == 2) {
            dto.setPassportNumber(passportNumber);
            if (image != null && !image.isEmpty()) {
                dto.setPassportImagePath(sauvegarderImage(image));
            }
        }

        return ResponseEntity.ok(clientService.modifierClient(id, dto));
    }

    // ── Créer client sans image (JSON pur) ─────────────────────
    @PostMapping("/json")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ClientDTO> creerJson(@RequestBody ClientDTO dto) {
        return ResponseEntity.ok(clientService.creerClient(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<ClientDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClient(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<List<ClientDTO>> getAll() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENTE')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        clientService.supprimerClient(id);
        return ResponseEntity.noContent().build();
    }

    // ── Sauvegarder l'image sur le serveur ─────────────────────
    private String sauvegarderImage(MultipartFile file) throws IOException {
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        String nomFichier = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path chemin = Paths.get(UPLOAD_DIR + nomFichier);
        Files.write(chemin, file.getBytes());
        return chemin.toString();
    }
}