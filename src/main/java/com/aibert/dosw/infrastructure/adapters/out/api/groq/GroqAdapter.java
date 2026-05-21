package com.aibert.dosw.infrastructure.adapters.out.api.groq;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import com.aibert.dosw.infrastructure.adapters.out.api.AbstractAIAdapter;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.aibert.dosw.infrastructure.adapters.out.api.mistral.MistralAdapter;
import com.aibert.dosw.infrastructure.adapters.out.feign.GroqAIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class GroqAdapter extends AbstractAIAdapter implements GenerativeAiPort {

    private final GroqAIClient groqAIClient;
    private final String groqApiKey;
    private final String groqModel;
    private final MistralAdapter mistralAdapter;

    public GroqAdapter(
            GroqAIClient groqAIClient,
            @Value("${groq.api.key}") String groqApiKey,
            @Value("${groq.api.model}") String groqModel,
            ObjectMapper objectMapper,
            @Qualifier("mistralAdapter") MistralAdapter mistralAdapter) {
        super(objectMapper);
        this.groqAIClient = groqAIClient;
        this.groqApiKey = groqApiKey != null ? groqApiKey.trim() : "";
        this.groqModel = groqModel;
        this.mistralAdapter = mistralAdapter;
    }

    @Override
    @CircuitBreaker(name = "groqAI", fallbackMethod = "fallbackToMistral")
    @Retry(name = "groqAI", fallbackMethod = "fallbackToMistral")
    public Recommendation generateRecommendation(String studentId, String enrichedContext, String requestType) {
        log.info("Generando recomendacion ({}) con Groq modelo '{}' para studentId={}", requestType, groqModel,
                studentId);

        GroqRequest request = GroqRequest.builder()
                .model(groqModel)
                .messages(List.of(
                        GroqRequest.Message.builder()
                                .role("system")
                                .content(
                                        "Eres AI.BERT, un tutor inteligente y compasivo para estudiantes universitarios. "
                                                +
                                                "Siempre respondes en formato JSON puro sin bloques markdown.")
                                .build(),
                        GroqRequest.Message.builder()
                                .role("user")
                                .content(buildPrompt(enrichedContext, requestType))
                                .build()))
                .maxTokens(800)
                .temperature(0.7)
                .build();

        GroqResponse response = groqAIClient.chatCompletion("Bearer " + groqApiKey, request);
        String generatedText = extractText(response);
        return parseAIResponse(studentId, generatedText, requestType);
    }

    @SuppressWarnings("unused")
    private Recommendation fallbackToMistral(String studentId, String enrichedContext, String requestType,
            Throwable throwable) {
        log.warn("Fallback Groq → Mistral activado para studentId={} debido a: {}", studentId, throwable.getMessage());
        return mistralAdapter.generateRecommendation(studentId, enrichedContext, requestType);
    }

    @Override
    @CircuitBreaker(name = "groqAI", fallbackMethod = "fallbackDailyPlanToMistral")
    @Retry(name = "groqAI", fallbackMethod = "fallbackDailyPlanToMistral")
    public List<ReorganizationSuggestionDTO> generateDailyPlanSuggestions(
            String studentId, String enrichedContext, LocalDate currentDate) {
        log.info("Generando sugerencias plan diario con Groq para studentId={}", studentId);

        GroqRequest request = GroqRequest.builder()
                .model(groqModel)
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

        GroqResponse response = groqAIClient.chatCompletion("Bearer " + groqApiKey, request);
        String generatedText = extractText(response);
        return parseDailyPlanSuggestions(studentId, generatedText, currentDate);
    }

    @SuppressWarnings("unused")
    private List<ReorganizationSuggestionDTO> fallbackDailyPlanToMistral(
            String studentId, String enrichedContext, LocalDate currentDate, Throwable throwable) {
        log.warn("Fallback Groq → Mistral para plan diario de studentId={} debido a: {}",
                studentId, throwable.getMessage());
        return mistralAdapter.generateDailyPlanSuggestions(studentId, enrichedContext, currentDate);
    }
}
