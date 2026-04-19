package com.example.telecom.controller;

import com.example.telecom.repository.*;
import com.example.telecom.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatsController {

    private final ClientRepository             clientRepository;
    private final ContratRepository            contratRepository;
    private final PromotionRepository          promotionRepository;
    private final SouscriptionPromotionRepository souscriptionRepository;
    private final UserRepository               userRepository;
    private final OffreRepository              offreRepository;
    private final ServiceRepository            serviceRepository;
    private final PlanTarifaireRepository      planTarifaireRepository;

    // ── VENTE ─────────────────────────────────────────────────
    @GetMapping("/vente")
    public ResponseEntity<Map<String, Object>> statsVente() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalClients   = clientRepository.count();
        long totalContrats  = contratRepository.count();
        long contratsActifs = contratRepository.findByStatut(Contrat.StatutContrat.ACTIF).size();
        long contratsRes    = contratRepository.findByStatut(Contrat.StatutContrat.RESILIE).size();
        long contratsSus    = contratRepository.findByStatut(Contrat.StatutContrat.SUSPENDU).size();
        long souscriptions  = souscriptionRepository.count();

        stats.put("totalClients",       totalClients);
        stats.put("totalContrats",      totalContrats);
        stats.put("contratsActifs",     contratsActifs);
        stats.put("contratsResilies",   contratsRes);
        stats.put("contratsSuspendus",  contratsSus);
        stats.put("totalSouscriptions", souscriptions);

        // Répartition contrats par statut
        List<Map<String,Object>> repartitionContrats = List.of(
                Map.of("statut","ACTIF",    "count", contratsActifs),
                Map.of("statut","RESILIE",  "count", contratsRes),
                Map.of("statut","SUSPENDU", "count", contratsSus)
        );
        stats.put("repartitionContrats", repartitionContrats);

        return ResponseEntity.ok(stats);
    }

    // ── METIER ────────────────────────────────────────────────
    @GetMapping("/metier")
    public ResponseEntity<Map<String, Object>> statsMetier() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalOffres     = offreRepository.count();
        long totalServices   = serviceRepository.count();
        long totalPlans      = planTarifaireRepository.count();
        long totalPromotions = promotionRepository.count();
        long promoEnAttente  = promotionRepository.findByStatut(Promotion.StatutPromotion.EN_ATTENTE).size();
        long promoValidee    = promotionRepository.findByStatut(Promotion.StatutPromotion.VALIDEE).size();
        long promoActive     = promotionRepository.findByStatut(Promotion.StatutPromotion.ACTIVE).size();
        long promoRejetee    = promotionRepository.findByStatut(Promotion.StatutPromotion.REJETEE).size();
        long promoSuspendue  = promotionRepository.findByStatut(Promotion.StatutPromotion.SUSPENDUE).size();

        stats.put("totalOffres",     totalOffres);
        stats.put("totalServices",   totalServices);
        stats.put("totalPlans",      totalPlans);
        stats.put("totalPromotions", totalPromotions);
        stats.put("promoEnAttente",  promoEnAttente);
        stats.put("promoValidee",    promoValidee);
        stats.put("promoActive",     promoActive);
        stats.put("promoRejetee",    promoRejetee);
        stats.put("promoSuspendue",  promoSuspendue);

        // Répartition promotions par statut
        List<Map<String,Object>> repartitionPromos = List.of(
                Map.of("statut","EN_ATTENTE", "count", promoEnAttente),
                Map.of("statut","VALIDEE",    "count", promoValidee),
                Map.of("statut","ACTIVE",     "count", promoActive),
                Map.of("statut","REJETEE",    "count", promoRejetee),
                Map.of("statut","SUSPENDUE",  "count", promoSuspendue)
        );
        stats.put("repartitionPromotions", repartitionPromos);

        return ResponseEntity.ok(stats);
    }

    // ── EXPLOIT ───────────────────────────────────────────────
    @GetMapping("/exploit")
    public ResponseEntity<Map<String, Object>> statsExploit() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long promoEnAttente = promotionRepository.findByStatut(Promotion.StatutPromotion.EN_ATTENTE).size();
        long promoValidee   = promotionRepository.findByStatut(Promotion.StatutPromotion.VALIDEE).size();
        long promoActive    = promotionRepository.findByStatut(Promotion.StatutPromotion.ACTIVE).size();
        long promoRejetee   = promotionRepository.findByStatut(Promotion.StatutPromotion.REJETEE).size();
        long promoSuspendue = promotionRepository.findByStatut(Promotion.StatutPromotion.SUSPENDUE).size();
        long totalSouscriptions = souscriptionRepository.count();

        stats.put("promoEnAttente",     promoEnAttente);
        stats.put("promoValidee",       promoValidee);
        stats.put("promoActive",        promoActive);
        stats.put("promoRejetee",       promoRejetee);
        stats.put("promoSuspendue",     promoSuspendue);
        stats.put("totalSouscriptions", totalSouscriptions);
        stats.put("totalPromotions",    promoEnAttente + promoValidee + promoActive + promoRejetee + promoSuspendue);

        List<Map<String,Object>> pipeline = List.of(
                Map.of("etape","En Attente", "count", promoEnAttente,  "couleur","#f59e0b"),
                Map.of("etape","Validée",    "count", promoValidee,    "couleur","#3b82f6"),
                Map.of("etape","Active",     "count", promoActive,     "couleur","#10b981"),
                Map.of("etape","Suspendue",  "count", promoSuspendue,  "couleur","#6b7280"),
                Map.of("etape","Rejetée",    "count", promoRejetee,    "couleur","#ef4444")
        );
        stats.put("pipelinePromotions", pipeline);

        return ResponseEntity.ok(stats);
    }

    // ── DSI ───────────────────────────────────────────────────
    @GetMapping("/dsi")
    public ResponseEntity<Map<String, Object>> statsDsi() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalUsers     = userRepository.count();
        long usersActifs    = userRepository.findAll().stream().filter(User::isEnabled).count();
        long usersInactifs  = totalUsers - usersActifs;
        long premiereConnexion = userRepository.findAll().stream().filter(u -> !u.isPremiereConnexion()).count();

        // Répartition par rôle
        Map<String,Long> parRole = new LinkedHashMap<>();
        userRepository.findAll().forEach(u -> {
            if (u.getRole() != null) {
                String role = u.getRole().name();
                parRole.put(role, parRole.getOrDefault(role, 0L) + 1);
            }
        });

        List<Map<String,Object>> repartitionRoles = new ArrayList<>();
        parRole.forEach((role, count) ->
                repartitionRoles.add(Map.of("role", role, "count", count))
        );

        stats.put("totalUsers",          totalUsers);
        stats.put("usersActifs",         usersActifs);
        stats.put("usersInactifs",       usersInactifs);
        stats.put("enAttenteConnexion",  premiereConnexion);
        stats.put("repartitionRoles",    repartitionRoles);
        stats.put("totalClients",        clientRepository.count());
        stats.put("totalContrats",       contratRepository.count());
        stats.put("totalPromotions",     promotionRepository.count());

        return ResponseEntity.ok(stats);
    }
}