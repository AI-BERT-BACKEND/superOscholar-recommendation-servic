package com.aibert.dosw.infrastructure.adapters.out.api.mistral;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import com.aibert.dosw.infrastructure.adapters.out.api.AbstractAIAdapter;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.MistralAIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("mistralAdapter")
public class MistralAdapter extends AbstractAIAdapter implements GenerativeAiPort {

    private final MistralAIClient mistralAIClient;
    private final String mistralApiKey;
    private final String mistralModel;

    public MistralAdapter(
            MistralAIClient mistralAIClient,
            @Value("${mistral.api.key}") String mistralApiKey,
            @Value("${mistral.api.model}") String mistralModel,
            ObjectMapper objectMapper) {
        super(objectMapper);
        this.mistralAIClient = mistralAIClient;
        this.mistralApiKey = mistralApiKey;
        this.mistralModel = mistralModel;
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
}
