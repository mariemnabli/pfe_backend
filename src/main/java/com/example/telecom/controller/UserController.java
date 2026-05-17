package com.example.telecom.controller;

import com.example.telecom.dto.CreateUserRequest;
import com.example.telecom.dto.UserDTO;
import com.example.telecom.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<UserDTO> creer(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.creerUtilisateur(request));
    }

    // DSI : Modifier un utilisateur
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<UserDTO> modifier(@PathVariable Long id,
                                            @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.modifierUtilisateur(id, request));
    }

    // DSI : Activer / Désactiver un compte
    @PutMapping("/{id}/toggle-actif")
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<UserDTO> toggleActif(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleActif(id));
    }

    // DSI : Supprimer un utilisateur
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        userService.supprimerUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    // DSI : Lister tous les utilisateurs
    @GetMapping
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(userService.getAllUtilisateurs());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> me(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }

    // DSI : Consulter un utilisateur
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }
}
