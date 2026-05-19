package com.aibert.dosw.infrastructure.adapters.out.api.mistral;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.MistralAIClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("mistralAdapter")
public class MistralAdapter implements GenerativeAiPort {

    private static final Logger log = LoggerFactory.getLogger(MistralAdapter.class);
    private static final String RECOMMENDATIONS_FIELD = "recommendations";
    private static final String CONFIDENCE_SCORE_FIELD = "confidenceScore";
    private static final String MOTIVATIONAL_MESSAGE_FIELD = "motivationalMessage";
    private static final String DEFAULT_MOTIVATION = "Tu puedes hacerlo.";

    private final MistralAIClient mistralAIClient;
    private final String mistralApiKey;
    private final String mistralModel;
    private final ObjectMapper objectMapper;

    public MistralAdapter(
            MistralAIClient mistralAIClient,
            @Value("${mistral.api.key}") String mistralApiKey,
            @Value("${mistral.api.model}") String mistralModel,
            ObjectMapper objectMapper) {
        this.mistralAIClient = mistralAIClient;
        this.mistralApiKey = mistralApiKey;
        this.mistralModel = mistralModel;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "mistralAI", fallbackMethod = "fallbackRecommendation")
    @Retry(name = "mistralAI", fallbackMethod = "fallbackRecommendation")
    public Recommendation generateRecommendation(String studentId, String enrichedContext, String requestType) {
        log.info("Generando recomendacion ({}) con Mistral modelo '{}' para studentId={}", requestType, mistralModel,
                studentId);

        GroqRequest request = GroqRequest.builder()
                .model(mistralModel)
                .messages(List.of(
                        GroqRequest.Message.builder()
                                .role("system")
                                .content(
                                        "Eres AI.BERT, un tutor inteligente y compasivo para estudiantes universitarios. "
                                                + "Siempre respondes en formato JSON puro sin bloques markdown.")
                                .build(),
                        GroqRequest.Message.builder()
                                .role("user")
                                .content(buildPrompt(enrichedContext, requestType))
                                .build()))
                .maxTokens(800)
                .temperature(0.7)
                .build();

        GroqResponse response = mistralAIClient.chatCompletion("Bearer " + mistralApiKey, request);
        String generatedText = extractText(response);
        return parseAIResponse(studentId, generatedText, requestType);
    }

    @SuppressWarnings("unused")
    private Recommendation fallbackRecommendation(String studentId, String enrichedContext, String requestType,
            Throwable throwable) {
        log.warn("Fallback estático activado (Mistral también falló) para studentId={} debido a: {}",
                studentId, throwable.getMessage());
        return Recommendation.builder()
                .studentId(studentId)
                .confidenceScore(0.0)
                .recommendationType(requestType)
                .motivationalMessage("El servicio de IA no está disponible en este momento. Intenta más tarde.")
                .recommendations(List.of(
                        Recommendation.RecommendationItem.builder()
                                .title("Servicio no disponible")
                                .description(
                                        "No se pudieron generar recomendaciones. Revisa tus apuntes y mantente constante.")
                                .type(requestType)
                                .itemScore(0.0)
                                .build()))
                .dateGenerated(LocalDate.now())
                .build();
    }

    // ── AIB-29: Sugerencias de plan diario ────────────────────────────────────

    @Override
    @CircuitBreaker(name = "mistralAI", fallbackMethod = "fallbackDailyPlanStatic")
    @Retry(name = "mistralAI", fallbackMethod = "fallbackDailyPlanStatic")
    public List<ReorganizationSuggestionDTO> generateDailyPlanSuggestions(
            String studentId, String enrichedContext, LocalDate currentDate) {
        log.info("Generando sugerencias plan diario con Mistral para studentId={}", studentId);

        GroqRequest request = GroqRequest.builder()
                .model(mistralModel)
                .messages(List.of(
                        GroqRequest.Message.builder()
                                .role("system")
                                .content("Eres AI.BERT, un tutor inteligente para estudiantes universitarios. "
                                        + "Siempre respondes en formato JSON puro sin bloques markdown.")
                                .build(),
                        GroqRequest.Message.builder()
                                .role("user")
                                .content(buildDailyPlanPrompt(enrichedContext, currentDate))
                                .build()))
                .maxTokens(600)
                .temperature(0.5)
                .build();

        GroqResponse response = mistralAIClient.chatCompletion("Bearer " + mistralApiKey, request);
        String generatedText = extractText(response);
        return parseDailyPlanSuggestions(studentId, generatedText, currentDate);
    }

    @SuppressWarnings("unused")
    private List<ReorganizationSuggestionDTO> fallbackDailyPlanStatic(
            String studentId, String enrichedContext, LocalDate currentDate, Throwable throwable) {
        log.warn("Fallback estático para plan diario de studentId={} debido a: {}",
                studentId, throwable.getMessage());
        return List.of();
    }

    private String buildDailyPlanPrompt(String enrichedContext, LocalDate currentDate) {
        return "Analiza las siguientes tareas reprogramables del estudiante para la fecha " + currentDate + ":\n\n" +
                enrichedContext + "\n\n" +
                "Genera hasta 5 sugerencias de reorganización indicando a qué día mover cada tarea. "
                + "Elige un toDay entre " + currentDate.plusDays(1) + " y " + currentDate.plusDays(7)
                + ". Justifica con datos reales del contexto.\n"
                + "Responde estrictamente en JSON puro:\n"
                + "{\n"
                + "  \"suggestions\": [\n"
                + "    {\n"
                + "      \"taskId\": \"id-de-la-tarea\",\n"
                + "      \"title\": \"Título de la tarea\",\n"
                + "      \"toDay\": \"YYYY-MM-DD\",\n"
                + "      \"justification\": \"Motivo concreto (máx 300 chars)\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
    }

    private List<ReorganizationSuggestionDTO> parseDailyPlanSuggestions(
            String studentId, String jsonText, LocalDate currentDate) {
        try {
            String cleanJson = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);
            JsonNode suggestionsNode = root.get("suggestions");
            if (suggestionsNode == null || !suggestionsNode.isArray()) {
                return List.of();
            }
            List<ReorganizationSuggestionDTO> result = new ArrayList<>();
            for (JsonNode node : suggestionsNode) {
                String taskId = readText(node, "taskId", "");
                String title = readText(node, "title", "Tarea");
                String toDayStr = readText(node, "toDay", "");
                LocalDate toDay;
                try {
                    toDay = toDayStr.isBlank() ? currentDate.plusDays(2) : LocalDate.parse(toDayStr);
                    if (!toDay.isAfter(currentDate))
                        toDay = currentDate.plusDays(2);
                } catch (Exception ex) {
                    toDay = currentDate.plusDays(2);
                }
                String justification = readText(node, "justification", "");
                if (justification.length() > 300)
                    justification = justification.substring(0, 300);
                result.add(ReorganizationSuggestionDTO.builder()
                        .taskId(taskId)
                        .title(title)
                        .fromDay(currentDate)
                        .toDay(toDay)
                        .justification(justification)
                        .build());
            }
            return result.stream().limit(5).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error parseando sugerencias de plan diario de Mistral para studentId={}: {}",
                    studentId, e.getMessage());
            return List.of();
        }
    }

    private String buildPrompt(String enrichedContext, String requestType) {
        return "Analiza el siguiente contexto del estudiante:\n\n" +
                enrichedContext + "\n\n" +
                "El estudiante solicito una recomendacion de tipo: " + requestType + ". " +
                "Responde estrictamente en JSON puro y genera entre 1 y 5 recomendaciones.\n" +
                "{\n" +
                "  \"" + CONFIDENCE_SCORE_FIELD + "\": 0.95,\n" +
                "  \"" + MOTIVATIONAL_MESSAGE_FIELD + "\": \"Mensaje motivacional basado en el contexto.\",\n" +
                "  \"" + RECOMMENDATIONS_FIELD + "\": [\n" +
                "    {\n" +
                "      \"title\": \"Titulo corto y accionable\",\n" +
                "      \"description\": \"Justificacion detallada basada en el contexto\",\n" +
                "      \"type\": \"" + requestType + "\",\n" +
                "      \"itemScore\": 0.9\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private String extractText(GroqResponse response) {
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            GroqResponse.Message message = response.getChoices().get(0).getMessage();
            if (message != null && message.getContent() != null) {
                return message.getContent();
            }
        }
        return "{}";
    }

    private Recommendation parseAIResponse(String studentId, String jsonText, String requestType) {
        try {
            String cleanJson = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);
            Double confidence = readDouble(root, CONFIDENCE_SCORE_FIELD, 0.0);
            String motivation = readText(root, MOTIVATIONAL_MESSAGE_FIELD, DEFAULT_MOTIVATION);
            List<Recommendation.RecommendationItem> items = parseRecommendationItems(root, requestType, confidence);
            return buildRecommendation(studentId, requestType, confidence, motivation, items);
        } catch (Exception e) {
            log.error("Error parseando respuesta de Mistral para studentId={}: {}", studentId, e.getMessage());
            return Recommendation.builder()
                    .studentId(studentId)
                    .confidenceScore(0.1)
                    .recommendationType(requestType)
                    .motivationalMessage("El servicio presenta problemas tecnicos, pero no te rindas.")
                    .recommendations(List.of(
                            Recommendation.RecommendationItem.builder()
                                    .title("Manten la calma")
                                    .description("Toma un descanso y revisa tus notas.")
                                    .type("GENERAL")
                                    .itemScore(0.1)
                                    .build()))
                    .dateGenerated(LocalDate.now())
                    .build();
        }
    }

    private Recommendation buildRecommendation(String studentId, String requestType, Double confidence,
            String message, List<Recommendation.RecommendationItem> items) {
        return Recommendation.builder()
                .studentId(studentId)
                .confidenceScore(confidence)
                .recommendationType(requestType)
                .motivationalMessage(message)
                .recommendations(items)
                .dateGenerated(LocalDate.now())
                .build();
    }

    private List<Recommendation.RecommendationItem> parseRecommendationItems(JsonNode root, String requestType,
            Double confidence) {
        List<Recommendation.RecommendationItem> items = new ArrayList<>();
        JsonNode recommendationsNode = root.get(RECOMMENDATIONS_FIELD);
        if (recommendationsNode == null || !recommendationsNode.isArray()) {
            return items;
        }
        for (JsonNode recNode : recommendationsNode) {
            items.add(Recommendation.RecommendationItem.builder()
                    .title(readText(recNode, "title", "Recomendacion"))
                    .description(readText(recNode, "description", ""))
                    .type(readText(recNode, "type", requestType))
                    .itemScore(readDouble(recNode, "itemScore", confidence))
                    .build());
        }
        return items;
    }

    private String readText(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.get(field);
        return node != null ? node.asText() : defaultValue;
    }

    private Double readDouble(JsonNode root, String field, Double defaultValue) {
        JsonNode node = root.get(field);
        return node != null ? node.asDouble() : defaultValue;
    }
}
