package com.example.telecom.service;

import com.example.telecom.dto.AssistantChatMessageDTO;
import com.example.telecom.dto.AssistantChatRequestDTO;
import com.example.telecom.dto.AssistantChatResponseDTO;
import com.example.telecom.entity.Offre;
import com.example.telecom.entity.PlanTarifaire;
import com.example.telecom.entity.Services;
import com.example.telecom.repository.OffreRepository;
import com.example.telecom.repository.PlanTarifaireRepository;
import com.example.telecom.repository.ServiceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private final OffreRepository offreRepository;
    private final PlanTarifaireRepository planTarifaireRepository;
    private final ServiceRepository serviceRepository;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String openApiKey;

    @Value("${openai.base-url}")
    private String openAiBaseUrl;

    @Value("${openai.model}")
    private String openAiModel;

    @Value("${assistant.catalog.max-offres:20}")
    private int maxOffres;

    @Value("${assistant.catalog.max-plans:20}")
    private int maxPlans;

    @Value("${assistant.catalog.max-services:40}")
    private int maxServices;

    public AssistantChatResponseDTO chat(AssistantChatRequestDTO request) {
        if (request == null || isBlank(request.getMessage())) {
            throw new RuntimeException("Le message du chatbot est obligatoire");
        }
        if (isBlank(openApiKey)) {
            throw new RuntimeException("La configuration openai.api-key est manquante");
        }

        List<Offre> offres = offreRepository.findAllWithCatalogDetails().stream()
                .limit(maxOffres)
                .toList();
        List<PlanTarifaire> plans = planTarifaireRepository.findAllByOrderByPrixMensuelAsc().stream()
                .limit(maxPlans)
                .toList();
        List<Services> services = serviceRepository.findAllByOrderByNomServiceAsc().stream()
                .limit(maxServices)
                .toList();

        String catalogContext = buildCatalogContext(offres, plans, services);
        String answer = callOpenAi(request, catalogContext);

        return AssistantChatResponseDTO.builder()
                .answer(answer)
                .offersUsed(offres.size())
                .plansUsed(plans.size())
                .servicesUsed(services.size())
                .model(openAiModel)
                .build();
    }

    private String callOpenAi(AssistantChatRequestDTO request, String catalogContext) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", openAiModel);

        // Construire le système prompt avec le catalogue
        String systemPrompt = """
                Tu es un assistant commercial pour une application telecom.
                Ta mission est d'aider le client a decouvrir les meilleures offres, les services disponibles
                et les plans tarifaires les plus adaptes a son besoin.
                Reponds en francais, de maniere claire, concise et actionnable.
                Base-toi uniquement sur le catalogue fourni. Si une information n'est pas presente, dis-le explicitement.
                Quand c'est pertinent, compare 2 ou 3 options en citant le prix mensuel, le type d'offre et les services inclus.
                
                Catalogue courant:
                """ + catalogContext;

        // Construire la liste des messages
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Ajouter l'historique
        if (request.getHistory() != null) {
            request.getHistory().stream()
                    .filter(Objects::nonNull)
                    .filter(message -> !isBlank(message.getRole()) && !isBlank(message.getContent()))
                    .limit(12)
                    .forEach(message -> messages.add(Map.of(
                            "role", normalizeRole(message.getRole()),
                            "content", message.getContent()
                    )));
        }

        // Ajouter le message actuel
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        payload.put("messages", messages);

        // Ajouter des paramètres optionnels
        payload.put("temperature", 0.7);
        payload.put("max_tokens", 1000);

        HttpRequest httpRequest;
        String openAiBaseUrl = "https://api.groq.com/openai/v1";

        try {
            httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(openAiBaseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openApiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
        } catch (IOException exception) {
            throw new RuntimeException("Impossible de serialiser la requete OpenAI: " + exception.getMessage());
        }

        HttpResponse<String> response;
        try {
            response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Appel OpenAI interrompu");
        } catch (IOException exception) {
            throw new RuntimeException("Erreur lors de l'appel au service OpenAI : " + exception.getMessage());
        }

        if (response.statusCode() >= 400) {
            throw new RuntimeException("OpenAI a retourne une erreur HTTP " + response.statusCode() + " : " + response.body());
        }

        return extractOutputText(response.body());
    }

    private List<Map<String, Object>> buildInputMessages(AssistantChatRequestDTO request) {
        List<Map<String, Object>> items = new ArrayList<>();

        if (request.getHistory() != null) {
            request.getHistory().stream()
                    .filter(Objects::nonNull)
                    .filter(message -> !isBlank(message.getRole()) && !isBlank(message.getContent()))
                    .limit(12)
                    .forEach(message -> items.add(toInputMessage(message.getRole(), message.getContent())));
        }

        items.add(toInputMessage("user", request.getMessage()));
        return items;
    }

    private Map<String, Object> toInputMessage(String role, String content) {
        return Map.of(
                "role", normalizeRole(role),
                "content", List.of(Map.of(
                        "type", "input_text",
                        "text", content
                ))
        );
    }

    private String buildCatalogContext(List<Offre> offres, List<PlanTarifaire> plans, List<Services> services) {
        String offersSection = offres.stream()
                .map(offre -> {
                    PlanTarifaire plan = offre.getPlanTarifaire();
                    String planLabel = plan == null
                            ? "aucun plan tarifaire associe"
                            : "%s (%.2f DT/mois, %s)".formatted(
                            safe(plan.getNom()),
                            plan.getPrixMensuel() == null ? 0D : plan.getPrixMensuel(),
                            safe(plan.getDescription())
                    );
                    String servicesLabel = offre.getServices() == null || offre.getServices().isEmpty()
                            ? "aucun service associe"
                            : offre.getServices().stream()
                              .map(service -> service.getNomService() + " - " + safe(service.getDescription()))
                              .collect(Collectors.joining(" | "));
                    return "- Offre #%d: %s | type: %s | plan: %s | services: %s".formatted(
                            offre.getId(),
                            safe(offre.getNomOffre()),
                            safe(offre.getTypeOffre()),
                            planLabel,
                            servicesLabel
                    );
                })
                .collect(Collectors.joining("\n"));

        String plansSection = plans.stream()
                .map(plan -> "- Plan #%d: %s | prix mensuel: %s DT | description: %s".formatted(
                        plan.getId(),
                        safe(plan.getNom()),
                        plan.getPrixMensuel() == null ? "-" : plan.getPrixMensuel(),
                        safe(plan.getDescription())
                ))
                .collect(Collectors.joining("\n"));

        String servicesSection = services.stream()
                .map(service -> "- Service #%d: %s | description: %s".formatted(
                        service.getId(),
                        safe(service.getNomService()),
                        safe(service.getDescription())
                ))
                .collect(Collectors.joining("\n"));

        return """
                [OFFRES]
                %s
                
                [PLANS TARIFAIRES]
                %s
                
                [SERVICES]
                %s
                """.formatted(emptyFallback(offersSection), emptyFallback(plansSection), emptyFallback(servicesSection));
    }

    private String extractOutputText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Format standard OpenAI/Groq
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null && !content.isNull()) {
                        String text = content.asText();
                        if (!isBlank(text)) {
                            return text;
                        }
                    }
                }
            }

            // Fallback pour d'autres formats
            JsonNode outputText = root.get("output_text");
            if (outputText != null && !outputText.isNull() && !isBlank(outputText.asText())) {
                return outputText.asText();
            }

            JsonNode output = root.get("output");
            if (output != null && output.isArray()) {
                StringBuilder answer = new StringBuilder();
                for (JsonNode item : output) {
                    JsonNode content = item.get("content");
                    if (content == null || !content.isArray()) {
                        continue;
                    }
                    for (JsonNode block : content) {
                        JsonNode text = block.get("text");
                        if (text != null && !isBlank(text.asText())) {
                            if (!answer.isEmpty()) {
                                answer.append('\n');
                            }
                            answer.append(text.asText());
                        }
                    }
                }
                if (!answer.isEmpty()) {
                    return answer.toString();
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException("Impossible de lire la reponse OpenAI: " + exception.getMessage());
        }

        throw new RuntimeException("La reponse OpenAI ne contient aucun texte exploitable");
    }

    private String normalizeRole(String role) {
        String normalized = role == null ? "" : role.trim().toLowerCase();
        return switch (normalized) {
            case "assistant", "system" -> normalized;
            default -> "user";
        };
    }

    private String safe(String value) {
        return isBlank(value) ? "-" : value.trim();
    }

    private String emptyFallback(String value) {
        return isBlank(value) ? "- aucun element disponible" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
