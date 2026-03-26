package com.example.telecom.controller;

import com.example.telecom.dto.CreateUserRequest;
import com.example.telecom.dto.UserDTO;
import com.example.telecom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    // DSI : Créer un utilisateur (envoi email automatique)
    @PostMapping
    public ResponseEntity<UserDTO> creer(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.creerUtilisateur(request));
    }

    // DSI : Modifier un utilisateur
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> modifier(@PathVariable Long id,
                                            @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.modifierUtilisateur(id, request));
    }

    // DSI : Activer / Désactiver un compte
    @PutMapping("/{id}/toggle-actif")
    public ResponseEntity<UserDTO> toggleActif(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleActif(id));
    }

    // DSI : Supprimer un utilisateur
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        userService.supprimerUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    // DSI : Lister tous les utilisateurs
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllUtilisateurs());
    }

    // DSI : Consulter un utilisateur
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}