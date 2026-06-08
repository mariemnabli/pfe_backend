package com.example.telecom.controller;

import com.example.telecom.repository.*;
import com.example.telecom.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    private final ReclamationRepository        reclamationRepository;

    // ── VENTE ─────────────────────────────────────────────────
    @GetMapping("/vente")
    @PreAuthorize("hasRole('VENTE')")
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
    @PreAuthorize("hasRole('METIER')")
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
    @PreAuthorize("hasRole('EXPLOIT')")
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
    @PreAuthorize("hasRole('DSI')")
    public ResponseEntity<Map<String, Object>> statsDsi() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long totalUsers     = userRepository.count();
        long usersActifs    = userRepository.findAll().stream().filter(User::isEnabled).count();
        long usersInactifs  = totalUsers - usersActifs;
        long premiereConnexion = userRepository.findAll().stream().filter(u -> !u.isPremiereConnexion()).count();
        long totalClients = clientRepository.count();
        long totalContrats = contratRepository.count();
        long totalReclamations = reclamationRepository.count();
        long totalPromotions = promotionRepository.count();

        List<Contrat> contrats = contratRepository.findAll();
        List<Reclamation> reclamations = reclamationRepository.findAll();
        List<Promotion> promotions = promotionRepository.findAll();

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

        List<Map<String,Object>> repartitionContrats = new ArrayList<>();
        for (ContractType type : ContractType.values()) {
            long count = contrats.stream()
                    .filter(contrat -> contrat.getContractType() == type)
                    .count();
            repartitionContrats.add(Map.of("contractType", type.name(), "count", count));
        }

        List<Map<String,Object>> repartitionContratsParStatut = new ArrayList<>();
        for (Contrat.StatutContrat statut : Contrat.StatutContrat.values()) {
            long count = contrats.stream()
                    .filter(contrat -> contrat.getStatut() == statut)
                    .count();
            repartitionContratsParStatut.add(Map.of("statut", statut.name(), "count", count));
        }

        List<Map<String,Object>> repartitionReclamations = new ArrayList<>();
        for (Reclamation.StatutReclamation statut : Reclamation.StatutReclamation.values()) {
            long count = reclamations.stream()
                    .filter(reclamation -> reclamation.getStatut() == statut)
                    .count();
            repartitionReclamations.add(Map.of("statut", statut.name(), "count", count));
        }

        List<Map<String,Object>> repartitionPromotions = new ArrayList<>();
        for (Promotion.StatutPromotion statut : Promotion.StatutPromotion.values()) {
            long count = promotions.stream()
                    .filter(promotion -> promotion.getStatut() == statut)
                    .count();
            repartitionPromotions.add(Map.of("statut", statut.name(), "count", count));
        }

        Map<LocalDate, Long> promotionsParDate = new TreeMap<>();
        for (Promotion promotion : promotions) {
            if (promotion.getDateDebut() != null) {
                LocalDate dateDebut = promotion.getDateDebut();
                promotionsParDate.put(dateDebut, promotionsParDate.getOrDefault(dateDebut, 0L) + 1);
            }
        }

        List<Map<String,Object>> promotionsParPeriode = new ArrayList<>();
        promotionsParDate.forEach((periode, count) ->
                promotionsParPeriode.add(Map.of("periode", periode, "count", count))
        );

        List<Map<String,Object>> promotionsParNombre = new ArrayList<>();
        promotions.stream()
                .sorted(Comparator.comparing(Promotion::getNomPromotion, Comparator.nullsLast(String::compareToIgnoreCase)))
                .forEach(promotion -> promotionsParNombre.add(Map.of(
                        "promotion", promotion.getNomPromotion() != null ? promotion.getNomPromotion() : "Sans nom",
                        "count", 1L
                )));

        stats.put("totalUsers",          totalUsers);
        stats.put("usersActifs",         usersActifs);
        stats.put("usersInactifs",       usersInactifs);
        stats.put("enAttenteConnexion",  premiereConnexion);
        stats.put("repartitionRoles",    repartitionRoles);
        stats.put("totalClients",        totalClients);
        stats.put("totalContrats",       totalContrats);
        stats.put("totalReclamations",   totalReclamations);
        stats.put("totalPromotions",     totalPromotions);
        stats.put("repartitionContrats", repartitionContrats);
        stats.put("repartitionContratsParStatut", repartitionContratsParStatut);
        stats.put("repartitionReclamations", repartitionReclamations);
        stats.put("repartitionPromotions", repartitionPromotions);
        stats.put("promotionsParPeriode", promotionsParPeriode);
        stats.put("promotionsParNombre", promotionsParNombre);

        return ResponseEntity.ok(stats);
    }
}
