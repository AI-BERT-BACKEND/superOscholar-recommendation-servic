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
        log.info("Generando recomendación ({}) con Groq modelo '{}' para studentId={}", requestType, groqModel, studentId);

        String prompt = buildPrompt(enrichedContext, requestType);

        GroqRequest request = GroqRequest.builder()
                .model(groqModel)
                .messages(List.of(
                        GroqRequest.Message.builder()
                                .role("system")
                                .content("Eres AI.BERT, un tutor inteligente y compasivo para estudiantes universitarios. " +
                                        "Siempre respondes en formato JSON puro sin bloques de código markdown.")
                                .build(),
                        GroqRequest.Message.builder()
                                .role("user")
                                .content(prompt)
                                .build()
                ))
                .max_tokens(800)
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
                .motivationalMessage("Nuestro asistente de IA no está disponible en este momento. " +
                        "Mientras tanto, recuerda revisar tus tareas pendientes y organizar tu tiempo. ¡Tú puedes!")
                .recommendations(List.of(
                        Recommendation.RecommendationItem.builder()
                                .title("Revisión General")
                                .description("Revisa tus notas más recientes y prioriza tareas cercanas.")
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
                "El estudiante ha solicitado una recomendación de tipo: " + requestType + ". " +
                "Debes devolver tu respuesta ESTRICTAMENTE en el siguiente formato JSON puro (sin bloques de código markdown, solo el JSON). " +
                "Debes generar entre 1 y 5 recomendaciones en el array.\n" +
                "{\n" +
                "  \"confidenceScore\": 0.95,\n" +
                "  \"motivationalMessage\": \"Tu mensaje motivacional aquí basado en el contexto.\",\n" +
                "  \"recommendations\": [\n" +
                "    {\n" +
                "      \"title\": \"Título corto y accionable\",\n" +
                "      \"description\": \"Justificación detallada basada en el contexto\",\n" +
                "      \"type\": \"" + requestType + "\",\n" +
                "      \"itemScore\": 0.9\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private String extractText(GroqResponse response) {
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return "{}";
    }

    private Recommendation parseAIResponse(Long studentId, String jsonText, String requestType) {
        try {
            String cleanJson = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            Double confidence = root.has("confidenceScore") ? root.get("confidenceScore").asDouble() : 0.0;
            String message = root.has("motivationalMessage") ? root.get("motivationalMessage").asText() : "¡Tú puedes!";

            List<Recommendation.RecommendationItem> items = new ArrayList<>();
            if (root.has("recommendations") && root.get("recommendations").isArray()) {
                for (JsonNode recNode : root.get("recommendations")) {
                    items.add(Recommendation.RecommendationItem.builder()
                            .title(recNode.has("title") ? recNode.get("title").asText() : "Recomendación")
                            .description(recNode.has("description") ? recNode.get("description").asText() : "")
                            .type(recNode.has("type") ? recNode.get("type").asText() : requestType)
                            .itemScore(recNode.has("itemScore") ? recNode.get("itemScore").asDouble() : confidence)
                            .build());
                }
            }

            return Recommendation.builder()
                    .studentId(studentId)
                    .confidenceScore(confidence)
                    .recommendationType(requestType)
                    .motivationalMessage(message)
                    .recommendations(items)
                    .dateGenerated(LocalDate.now())
                    .build();
        } catch (Exception e) {
            log.error("Error parseando respuesta de Groq para studentId={}: {}", studentId, e.getMessage());
            return Recommendation.builder()
                    .studentId(studentId)
                    .confidenceScore(0.1)
                    .recommendationType(requestType)
                    .motivationalMessage("El servicio está experimentando problemas técnicos, pero no te rindas.")
                    .recommendations(List.of(
                            Recommendation.RecommendationItem.builder()
                                    .title("Mantén la calma")
                                    .description("Toma un descanso y revisa tus notas.")
                                    .type("GENERAL")
                                    .itemScore(0.1)
                                    .build()
                    ))
                    .dateGenerated(LocalDate.now())
                    .build();
        }
    }
}
