package com.example.telecom.service;

import com.example.telecom.dto.CreateUserRequest;
import com.example.telecom.dto.UserDTO;
import com.example.telecom.entity.User;
import com.example.telecom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;
    private final EmailService       emailService;

    // ── DSI : Créer un utilisateur ─────────────────────────────
    // Génère un mot de passe aléatoire si non fourni,
    // envoie les credentials par email, actif = false par défaut
    public UserDTO creerUtilisateur(CreateUserRequest request) {

        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé : " + request.getEmail());
        }

        // Générer un mot de passe aléatoire si le DSI n'en fournit pas
        String motDePasseBrut = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : genererMotDePasse();

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setPassword(passwordEncoder.encode(motDePasseBrut));
        user.setEnabled(true);                  // compte actif dès la création
        user.setPremiereConnexion(false);      // pas encore connecté

        userRepository.save(user);

        // Envoyer l'email avec les credentials en clair
        emailService.envoyerCredentials(user.getEmail(), user.getUsername(), motDePasseBrut);

        return toDTO(user);
    }

    // ── DSI : Modifier un utilisateur ─────────────────────────
    public UserDTO modifierUtilisateur(Long id, CreateUserRequest request) {
        User user = getUser(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return toDTO(userRepository.save(user));
    }

    // ── DSI : Activer / Désactiver un compte ───────────────────
    public UserDTO toggleActif(Long id) {
        User user = getUser(id);
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        emailService.envoyerNotificationCompte(user.getEmail(), user.getUsername(), user.isEnabled());
        return toDTO(user);
    }

    // ── DSI : Supprimer un utilisateur ────────────────────────
    public void supprimerUtilisateur(Long id) {
        userRepository.deleteById(id);
    }

    // ── DSI : Lister tous les utilisateurs ────────────────────
    public List<UserDTO> getAllUtilisateurs() {
        return userRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── DSI : Consulter un utilisateur ────────────────────────
    public UserDTO getById(Long id) {
        return toDTO(getUser(id));
    }

    // ── Privé : récupérer entité User ──────────────────────────
    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + id));
    }

    // ── Privé : générer mot de passe aléatoire 10 chars ───────
    //  Format : 2 maj + 2 chiffres + 2 spéciaux + 4 min
    private String genererMotDePasse() {
        String majuscules = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        String minuscules = "abcdefghjkmnpqrstuvwxyz";
        String chiffres   = "23456789";
        String speciaux   = "@#$%&*!";

        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();

        for (int i = 0; i < 2; i++) sb.append(majuscules.charAt(rnd.nextInt(majuscules.length())));
        for (int i = 0; i < 2; i++) sb.append(chiffres.charAt(rnd.nextInt(chiffres.length())));
        for (int i = 0; i < 2; i++) sb.append(speciaux.charAt(rnd.nextInt(speciaux.length())));
        for (int i = 0; i < 4; i++) sb.append(minuscules.charAt(rnd.nextInt(minuscules.length())));

        // Mélanger les caractères
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            char tmp = chars[i]; chars[i] = chars[j]; chars[j] = tmp;
        }
        return new String(chars);
    }

    // ── Mapper entité → DTO ────────────────────────────────────
    private UserDTO toDTO(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .role(u.getRole())
                .enabled(u.isEnabled())
                .premiereConnexion(u.isPremiereConnexion())
                .firstTimeConnexion(u.getFirstTimeConnexion())
                .build();
    }
}