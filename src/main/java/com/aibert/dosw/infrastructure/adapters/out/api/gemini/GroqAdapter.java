package com.aibert.dosw.infrastructure.adapters.out.api.gemini;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GroqResponse;
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

/**
 * Adaptador de IA usando Groq (gratuito, OpenAI-compatible).
 * Se activa cuando ai.provider=groq en application.yml.
 * Usa modelos como llama-3.3-70b-versatile para generar recomendaciones.
 */
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
    @CircuitBreaker(name = "geminiAI", fallbackMethod = "fallbackRecommendation")
    @Retry(name = "geminiAI", fallbackMethod = "fallbackRecommendation")
    public Recommendation generateRecommendation(Long studentId, String enrichedContext) {
        log.info("Generando recomendación con Groq modelo '{}' para studentId={}", groqModel, studentId);

        String prompt = buildPrompt(enrichedContext);

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
                .max_tokens(500)
                .temperature(0.7)
                .build();

        GroqResponse response = groqAIClient.chatCompletion("Bearer " + groqApiKey, request);
        String generatedText = extractText(response);

        return parseAIResponse(studentId, generatedText);
    }

    /**
     * Fallback cuando la IA falla (timeout, error HTTP, circuit breaker abierto, etc.).
     * Retorna un mensaje controlado sin depender de la IA.
     */
    @SuppressWarnings("unused")
    private Recommendation fallbackRecommendation(Long studentId, String enrichedContext, Throwable throwable) {
        log.warn("Fallback Groq activado para studentId={} debido a: {}", studentId, throwable.getMessage());
        return Recommendation.builder()
                .studentId(studentId)
                .confidenceScore(0.0)
                .motivationalMessage("Nuestro asistente de IA no está disponible en este momento. " +
                        "Mientras tanto, recuerda revisar tus tareas pendientes y organizar tu tiempo. ¡Tú puedes!")
                .studyTips(List.of(
                        "Revisa tus notas más recientes",
                        "Prioriza las tareas con fecha de entrega más cercana",
                        "Toma descansos de 5 minutos cada 25 minutos de estudio (Técnica Pomodoro)"
                ))
                .dateGenerated(LocalDate.now())
                .build();
    }

    private String buildPrompt(String enrichedContext) {
        return "Analiza el siguiente contexto del estudiante:\n\n" +
                enrichedContext + "\n\n" +
                "Debes devolver tu respuesta ESTRICTAMENTE en el siguiente formato JSON puro (sin bloques de código markdown, solo el JSON):\n" +
                "{\n" +
                "  \"confidenceScore\": 0.95,\n" +
                "  \"motivationalMessage\": \"Tu mensaje motivacional aquí basado en el contexto.\",\n" +
                "  \"studyTips\": [\"Tip 1\", \"Tip 2\", \"Tip 3\"]\n" +
                "}";
    }

    private String extractText(GroqResponse response) {
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return "{}";
    }

    private Recommendation parseAIResponse(Long studentId, String jsonText) {
        try {
            String cleanJson = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);

            Double confidence = root.has("confidenceScore") ? root.get("confidenceScore").asDouble() : 0.0;
            String message = root.has("motivationalMessage") ? root.get("motivationalMessage").asText() : "¡Tú puedes!";

            List<String> tips = new ArrayList<>();
            if (root.has("studyTips") && root.get("studyTips").isArray()) {
                for (JsonNode tip : root.get("studyTips")) {
                    tips.add(tip.asText());
                }
            }

            return Recommendation.builder()
                    .studentId(studentId)
                    .confidenceScore(confidence)
                    .motivationalMessage(message)
                    .studyTips(tips)
                    .dateGenerated(LocalDate.now())
                    .build();
        } catch (Exception e) {
            log.error("Error parseando respuesta de Groq para studentId={}: {}", studentId, e.getMessage());
            return Recommendation.builder()
                    .studentId(studentId)
                    .confidenceScore(0.1)
                    .motivationalMessage("El servicio está experimentando problemas técnicos, pero no te rindas.")
                    .studyTips(List.of("Toma un descanso", "Revisa tus notas"))
                    .dateGenerated(LocalDate.now())
                    .build();
        }
    }
}
