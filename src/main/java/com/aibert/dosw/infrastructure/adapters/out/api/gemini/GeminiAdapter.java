package com.aibert.dosw.infrastructure.adapters.out.api.gemini;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.GeminiAIClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador para la API de Gemini (Google Generative Language API).
 * Actúa como proveedor secundario de IA: se invoca cuando Groq falla
 * (fallback).
 * Implementa Circuit Breaker para resiliencia (AIB-28).
 */
@Component
public class GeminiAdapter implements GenerativeAiPort {

    private static final Logger log = LoggerFactory.getLogger(GeminiAdapter.class);
    private static final String RECOMMENDATIONS_FIELD = "recommendations";
    private static final String CONFIDENCE_SCORE_FIELD = "confidenceScore";
    private static final String MOTIVATIONAL_MESSAGE_FIELD = "motivationalMessage";
    private static final String DEFAULT_MOTIVATION = "Cada esfuerzo que haces te acerca a tus metas.";

    private final GeminiAIClient geminiAIClient;
    private final String geminiApiKey;
    private final String geminiModel;
    private final ObjectMapper objectMapper;

    public GeminiAdapter(
            GeminiAIClient geminiAIClient,
            @Value("${gemini.api.key}") String geminiApiKey,
            @Value("${gemini.api.model}") String geminiModel,
            ObjectMapper objectMapper) {
        this.geminiAIClient = geminiAIClient;
        this.geminiApiKey = geminiApiKey;
        this.geminiModel = geminiModel;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "geminiAI", fallbackMethod = "fallbackRecommendation")
    public Recommendation generateRecommendation(String studentId, String enrichedContext, String requestType) {
        log.info("Generando recomendacion ({}) con Gemini modelo '{}' para studentId={}", requestType, geminiModel,
                studentId);

        String prompt = buildPrompt(enrichedContext, requestType);

        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .role("user")
                                .parts(List.of(GeminiRequest.Part.builder().text(prompt).build()))
                                .build()))
                .generationConfig(GeminiRequest.GenerationConfig.builder()
                        .maxOutputTokens(800)
                        .temperature(0.7)
                        .build())
                .build();

        GeminiResponse response = geminiAIClient.generateContent(geminiModel, geminiApiKey, request);
        String generatedText = extractText(response);
        return parseAIResponse(studentId, generatedText, requestType);
    }

    @SuppressWarnings("unused")
    private Recommendation fallbackRecommendation(String studentId, String enrichedContext, String requestType,
            Throwable throwable) {
        log.warn("Fallback Gemini activado para studentId={} debido a: {}", studentId, throwable.getMessage());
        return Recommendation.builder()
                .studentId(studentId)
                .confidenceScore(0.0)
                .recommendationType(requestType)
                .motivationalMessage("Nuestros asistentes de IA no están disponibles en este momento. " +
                        "Mientras tanto, revisa tus tareas pendientes y organiza tu tiempo.")
                .recommendations(List.of(
                        Recommendation.RecommendationItem.builder()
                                .title("Revision general")
                                .description("Revisa tus notas más recientes y prioriza tareas cercanas.")
                                .type("GENERAL")
                                .itemScore(0.5)
                                .build()))
                .dateGenerated(LocalDate.now())
                .build();
    }

    private String buildPrompt(String enrichedContext, String requestType) {
        return "Eres AI.BERT, un tutor inteligente y compasivo para estudiantes universitarios. " +
                "Siempre respondes en formato JSON puro sin bloques markdown.\n\n" +
                "Analiza el siguiente contexto del estudiante:\n\n" +
                enrichedContext + "\n\n" +
                "El estudiante solicitó una recomendación de tipo: " + requestType + ". " +
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

    private String extractText(GeminiResponse response) {
        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            GeminiResponse.Candidate candidate = response.getCandidates().get(0);
            if (candidate.getContent() != null
                    && candidate.getContent().getParts() != null
                    && !candidate.getContent().getParts().isEmpty()) {
                String text = candidate.getContent().getParts().get(0).getText();
                if (text != null) {
                    return text;
                }
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
            log.error("Error parseando respuesta de Gemini para studentId={}: {}", studentId, e.getMessage());
            return Recommendation.builder()
                    .studentId(studentId)
                    .confidenceScore(0.1)
                    .recommendationType(requestType)
                    .motivationalMessage("El servicio presenta problemas técnicos, pero no te rindas.")
                    .recommendations(List.of(
                            Recommendation.RecommendationItem.builder()
                                    .title("Mantén la calma")
                                    .description("Toma un descanso y revisa tus notas.")
                                    .type("GENERAL")
                                    .itemScore(0.1)
                                    .build()))
                    .dateGenerated(LocalDate.now())
                    .build();
        }
    }

    private Recommendation buildRecommendation(
            String studentId, String requestType, Double confidence,
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

    private List<Recommendation.RecommendationItem> parseRecommendationItems(
            JsonNode root, String requestType, Double confidence) {
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
