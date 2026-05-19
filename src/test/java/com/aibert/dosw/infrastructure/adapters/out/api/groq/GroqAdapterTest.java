package com.aibert.dosw.infrastructure.adapters.out.api.groq;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.aibert.dosw.infrastructure.adapters.out.api.mistral.MistralAdapter;
import com.aibert.dosw.infrastructure.adapters.out.feign.GroqAIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroqAdapterTest {

    @Test
    void generateRecommendation_withValidJson_buildsRecommendationFromGroqResponse() {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        GroqResponse response = responseWithContent("""
                {
                  "confidenceScore": 0.93,
                  "motivationalMessage": "Vas bien, sigue asi.",
                  "recommendations": [
                    {
                      "title": "Enfocate en calculo",
                      "description": "Prioriza ejercicios tipo parcial.",
                      "type": "ACADEMIC",
                      "itemScore": 0.88
                    }
                  ]
                }
                """);
        when(groqAIClient.chatCompletion(eq("Bearer test-key"), any(GroqRequest.class))).thenReturn(response);

        Recommendation result = adapter.generateRecommendation("10", "context", "ACADEMIC");

        assertEquals("10", result.getStudentId());
        assertEquals(0.93, result.getConfidenceScore());
        assertEquals("ACADEMIC", result.getRecommendationType());
        assertEquals(1, result.getRecommendations().size());
        assertEquals("Enfocate en calculo", result.getRecommendations().get(0).getTitle());

        ArgumentCaptor<GroqRequest> requestCaptor = ArgumentCaptor.forClass(GroqRequest.class);
        verify(groqAIClient).chatCompletion(eq("Bearer test-key"), requestCaptor.capture());
        GroqRequest sentRequest = requestCaptor.getValue();
        assertEquals("llama-test", sentRequest.getModel());
        assertEquals(800, sentRequest.getMaxTokens());
        assertEquals(2, sentRequest.getMessages().size());
    }

    @Test
    void generateRecommendation_withMalformedJson_returnsTechnicalFallback() {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        when(groqAIClient.chatCompletion(eq("Bearer test-key"), any(GroqRequest.class)))
                .thenReturn(responseWithContent("{not-json"));

        Recommendation result = adapter.generateRecommendation("55", "context", "GENERAL");

        assertEquals("55", result.getStudentId());
        assertEquals(0.1, result.getConfidenceScore());
        assertEquals("GENERAL", result.getRecommendations().get(0).getType());
        assertTrue(result.getMotivationalMessage().contains("problemas tecnicos"));
    }

    @Test
    void generateRecommendation_withEmptyChoices_returnsDefaultsFromParser() {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        GroqResponse emptyResponse = new GroqResponse();
        when(groqAIClient.chatCompletion(eq("Bearer test-key"), any(GroqRequest.class))).thenReturn(emptyResponse);

        Recommendation result = adapter.generateRecommendation("77", "context", "TIME_MANAGEMENT");

        assertEquals("77", result.getStudentId());
        assertEquals(0.0, result.getConfidenceScore());
        assertTrue(result.getRecommendations().isEmpty());
    }

    @Test
    void generateRecommendation_withNullMessageContent_returnsDefaultsFromParser() {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        GroqResponse.Message message = new GroqResponse.Message();
        message.setRole("assistant");
        message.setContent(null);

        GroqResponse.Choice choice = new GroqResponse.Choice();
        choice.setMessage(message);

        GroqResponse response = new GroqResponse();
        response.setChoices(List.of(choice));

        when(groqAIClient.chatCompletion(eq("Bearer test-key"), any(GroqRequest.class))).thenReturn(response);

        Recommendation result = adapter.generateRecommendation("91", "context", "GENERAL");
        assertEquals(0.0, result.getConfidenceScore());
        assertTrue(result.getRecommendations().isEmpty());
    }

    @Test
    void fallbackToMistral_delegatesToMistralAdapter() throws Exception {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        Recommendation mistralResult = Recommendation.builder()
                .studentId("22")
                .confidenceScore(0.75)
                .recommendationType("GENERAL")
                .motivationalMessage("Mistral responde")
                .recommendations(List.of())
                .build();
        when(mistralAdapter.generateRecommendation("22", "ctx", "GENERAL")).thenReturn(mistralResult);

        Method method = GroqAdapter.class.getDeclaredMethod(
                "fallbackToMistral", String.class, String.class, String.class, Throwable.class);
        method.setAccessible(true);

        Recommendation result = (Recommendation) method.invoke(
                adapter, "22", "ctx", "GENERAL", new RuntimeException("groq down"));

        assertEquals("22", result.getStudentId());
        assertEquals(0.75, result.getConfidenceScore());
        verify(mistralAdapter).generateRecommendation("22", "ctx", "GENERAL");
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
