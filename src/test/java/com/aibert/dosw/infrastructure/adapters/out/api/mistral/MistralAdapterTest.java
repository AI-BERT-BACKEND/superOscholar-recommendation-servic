package com.aibert.dosw.infrastructure.adapters.out.api.mistral;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.MistralAIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MistralAdapterTest {

    @Test
    void generateRecommendation_withValidJson_buildsRecommendationFromMistralResponse() {
        MistralAIClient mistralAIClient = mock(MistralAIClient.class);
        MistralAdapter adapter = new MistralAdapter(mistralAIClient, "test-mistral-key", "mistral-small-latest",
                new ObjectMapper());

        GroqResponse response = responseWithContent("""
                {
                  "confidenceScore": 0.88,
                  "motivationalMessage": "Sigue adelante, puedes lograrlo.",
                  "recommendations": [
                    {
                      "title": "Practica ejercicios de algebra",
                      "description": "Dedica 30 minutos diarios a ejercicios tipo parcial.",
                      "type": "ACADEMIC",
                      "itemScore": 0.85
                    }
                  ]
                }
                """);
        when(mistralAIClient.chatCompletion(eq("Bearer test-mistral-key"), any(GroqRequest.class)))
                .thenReturn(response);

        Recommendation result = adapter.generateRecommendation("20", "context", "ACADEMIC");

        assertEquals("20", result.getStudentId());
        assertEquals(0.88, result.getConfidenceScore());
        assertEquals("ACADEMIC", result.getRecommendationType());
        assertEquals(1, result.getRecommendations().size());
        assertEquals("Practica ejercicios de algebra", result.getRecommendations().get(0).getTitle());

        ArgumentCaptor<GroqRequest> requestCaptor = ArgumentCaptor.forClass(GroqRequest.class);
        verify(mistralAIClient).chatCompletion(eq("Bearer test-mistral-key"), requestCaptor.capture());
        GroqRequest sentRequest = requestCaptor.getValue();
        assertEquals("mistral-small-latest", sentRequest.getModel());
        assertEquals(800, sentRequest.getMaxTokens());
        assertEquals(2, sentRequest.getMessages().size());
    }

    @Test
    void generateRecommendation_withMalformedJson_returnsTechnicalFallback() {
        MistralAIClient mistralAIClient = mock(MistralAIClient.class);
        MistralAdapter adapter = new MistralAdapter(mistralAIClient, "test-mistral-key", "mistral-small-latest",
                new ObjectMapper());

        when(mistralAIClient.chatCompletion(eq("Bearer test-mistral-key"), any(GroqRequest.class)))
                .thenReturn(responseWithContent("{not-json"));

        Recommendation result = adapter.generateRecommendation("55", "context", "GENERAL");

        assertEquals("55", result.getStudentId());
        assertEquals(0.1, result.getConfidenceScore());
        assertEquals("GENERAL", result.getRecommendations().get(0).getType());
        assertTrue(result.getMotivationalMessage().contains("problemas tecnicos"));
    }

    @Test
    void generateRecommendation_withEmptyChoices_returnsDefaultsFromParser() {
        MistralAIClient mistralAIClient = mock(MistralAIClient.class);
        MistralAdapter adapter = new MistralAdapter(mistralAIClient, "test-mistral-key", "mistral-small-latest",
                new ObjectMapper());

        GroqResponse emptyResponse = new GroqResponse();
        when(mistralAIClient.chatCompletion(eq("Bearer test-mistral-key"), any(GroqRequest.class)))
                .thenReturn(emptyResponse);

        Recommendation result = adapter.generateRecommendation("77", "context", "TIME_MANAGEMENT");

        assertEquals("77", result.getStudentId());
        assertEquals(0.0, result.getConfidenceScore());
        assertTrue(result.getRecommendations().isEmpty());
    }

    @Test
    void generateRecommendation_withNullMessageContent_returnsDefaultsFromParser() {
        MistralAIClient mistralAIClient = mock(MistralAIClient.class);
        MistralAdapter adapter = new MistralAdapter(mistralAIClient, "test-mistral-key", "mistral-small-latest",
                new ObjectMapper());

        GroqResponse.Message message = new GroqResponse.Message();
        message.setRole("assistant");
        message.setContent(null);

        GroqResponse.Choice choice = new GroqResponse.Choice();
        choice.setMessage(message);

        GroqResponse response = new GroqResponse();
        response.setChoices(List.of(choice));

        when(mistralAIClient.chatCompletion(eq("Bearer test-mistral-key"), any(GroqRequest.class)))
                .thenReturn(response);

        Recommendation result = adapter.generateRecommendation("91", "context", "GENERAL");
        assertEquals(0.0, result.getConfidenceScore());
        assertTrue(result.getRecommendations().isEmpty());
    }

    @Test
    void fallbackRecommendation_returnsStaticFallbackWithStudentId() throws Exception {
        MistralAIClient mistralAIClient = mock(MistralAIClient.class);
        MistralAdapter adapter = new MistralAdapter(mistralAIClient, "test-mistral-key", "mistral-small-latest",
                new ObjectMapper());

        Method method = MistralAdapter.class.getDeclaredMethod(
                "fallbackRecommendation", String.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);

        Recommendation fallback = (Recommendation) method.invoke(
                adapter, "33", "ctx", "GENERAL", new RuntimeException("mistral down"));

        assertEquals("33", fallback.getStudentId());
        assertEquals(0.0, fallback.getConfidenceScore());
        assertEquals("GENERAL", fallback.getRecommendationType());
        assertEquals(1, fallback.getRecommendations().size());
        assertDoesNotThrow(fallback::toString);
    }

    private GroqResponse responseWithContent(String content) {
        GroqResponse.Message message = new GroqResponse.Message();
        message.setRole("assistant");
        message.setContent(content);

        GroqResponse.Choice choice = new GroqResponse.Choice();
        choice.setMessage(message);

        GroqResponse response = new GroqResponse();
        response.setChoices(List.of(choice));
        return response;
    }
}
