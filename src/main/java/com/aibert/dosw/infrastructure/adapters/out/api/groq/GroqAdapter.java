package com.aibert.dosw.infrastructure.adapters.out.api.groq;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.GroqAIClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
@ConditionalOnProperty(name = "ai.provider", havingValue = "groq")
public class GroqAdapter implements GenerativeAiPort {

    private static final Logger log = LoggerFactory.getLogger(GroqAdapter.class);
    private static final String RECOMMENDATIONS_FIELD = "recommendations";
    private static final String CONFIDENCE_SCORE_FIELD = "confidenceScore";
    private static final String MOTIVATIONAL_MESSAGE_FIELD = "motivationalMessage";
    private static final String DEFAULT_MOTIVATION = "Tu puedes hacerlo.";

    private final GroqAIClient groqAIClient;
    private final String groqApiKey;
    private final String groqModel;
    private final ObjectMapper objectMapper;

    public GroqAdapter(
            GroqAIClient groqAIClient,
            @Value("${groq.api.key}") String groqApiKey,
            @Value("${groq.api.model}") String groqModel,
            ObjectMapper objectMapper) {
        this.groqAIClient = groqAIClient;
        this.groqApiKey = groqApiKey;
        this.groqModel = groqModel;
        this.objectMapper = objectMapper;
    }

    @Override
    @CircuitBreaker(name = "groqAI", fallbackMethod = "fallbackRecommendation")
    @Retry(name = "groqAI", fallbackMethod = "fallbackRecommendation")
    public Recommendation generateRecommendation(Long studentId, String enrichedContext, String requestType) {
        log.info("Generando recomendacion ({}) con Groq modelo '{}' para studentId={}", requestType, groqModel, studentId);

        GroqRequest request = GroqRequest.builder()
                .model(groqModel)
                .messages(List.of(
                        GroqRequest.Message.builder()
                                .role("system")
                                .content("Eres AI.BERT, un tutor inteligente y compasivo para estudiantes universitarios. " +
                                        "Siempre respondes en formato JSON puro sin bloques markdown.")
                                .build(),
                        GroqRequest.Message.builder()
                                .role("user")
                                .content(buildPrompt(enrichedContext, requestType))
                                .build()
                ))
                .maxTokens(800)
                .temperature(0.7)
                .build();

        GroqResponse response = groqAIClient.chatCompletion("Bearer " + groqApiKey, request);
        String generatedText = extractText(response);
        return parseAIResponse(studentId, generatedText, requestType);
    }

    @SuppressWarnings("unused")
    private Recommendation fallbackRecommendation(Long studentId, String enrichedContext, String requestType, Throwable throwable) {
        log.warn("Fallback Groq activado para studentId={} debido a: {}", studentId, throwable.getMessage());
        return Recommendation.builder()
                .studentId(studentId)
                .confidenceScore(0.0)
                .recommendationType(requestType)
                .motivationalMessage("Nuestro asistente de IA no esta disponible en este momento. " +
                        "Mientras tanto, revisa tus tareas pendientes y organiza tu tiempo.")
                .recommendations(List.of(
                        Recommendation.RecommendationItem.builder()
                                .title("Revision general")
                                .description("Revisa tus notas mas recientes y prioriza tareas cercanas.")
                                .type("GENERAL")
                                .itemScore(0.5)
                                .build()
                ))
                .dateGenerated(LocalDate.now())
                .build();
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

    private Recommendation parseAIResponse(Long studentId, String jsonText, String requestType) {
        try {
            String cleanJson = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);
            Double confidence = readDouble(root, CONFIDENCE_SCORE_FIELD, 0.0);
            String motivation = readText(root, MOTIVATIONAL_MESSAGE_FIELD, DEFAULT_MOTIVATION);
            List<Recommendation.RecommendationItem> items = parseRecommendationItems(root, requestType, confidence);
            return buildRecommendation(studentId, requestType, confidence, motivation, items);
        } catch (Exception e) {
            log.error("Error parseando respuesta de Groq para studentId={}: {}", studentId, e.getMessage());
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
                                    .build()
                    ))
                    .dateGenerated(LocalDate.now())
                    .build();
        }
    }

    private Recommendation buildRecommendation(
            Long studentId,
            String requestType,
            Double confidence,
            String message,
            List<Recommendation.RecommendationItem> items) {
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
            JsonNode root,
            String requestType,
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
