package com.aibert.dosw.infrastructure.adapters.out.api.groq;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.aibert.dosw.infrastructure.adapters.out.api.mistral.MistralAdapter;
import com.aibert.dosw.infrastructure.adapters.out.feign.GroqAIClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void generateDailyPlanSuggestions_withValidJson_appliesNormalizationAndLimit() {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);
        LocalDate currentDate = LocalDate.of(2026, 5, 20);

        String longJustification = "x".repeat(320);
        GroqResponse response = responseWithContent("""
                {
                  "suggestions": [
                    {"taskId":"t1","title":"T1","toDay":"2026-05-19","justification":"j1"},
                    {"taskId":"t2","title":"T2","toDay":"","justification":"j2"},
                    {"taskId":"t3","title":"T3","toDay":"bad-date","justification":"j3"},
                    {"taskId":"t4","title":"T4","toDay":"2026-05-23","justification":"j4"},
                    {"taskId":"t5","title":"T5","toDay":"2026-05-24","justification":"%s"},
                    {"taskId":"t6","title":"T6","toDay":"2026-05-25","justification":"j6"}
                  ]
                }
                """.formatted(longJustification));
        when(groqAIClient.chatCompletion(eq("Bearer test-key"), any(GroqRequest.class))).thenReturn(response);

        List<ReorganizationSuggestionDTO> result = adapter.generateDailyPlanSuggestions("10", "context", currentDate);

        assertEquals(5, result.size());
        assertEquals(currentDate.plusDays(2), result.get(0).getToDay());
        assertEquals(currentDate.plusDays(2), result.get(1).getToDay());
        assertEquals(currentDate.plusDays(2), result.get(2).getToDay());
        assertEquals(LocalDate.of(2026, 5, 23), result.get(3).getToDay());
        assertEquals(300, result.get(4).getJustification().length());
    }

    @Test
    void generateDailyPlanSuggestions_withNonArraySuggestions_returnsEmptyList() {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        when(groqAIClient.chatCompletion(eq("Bearer test-key"), any(GroqRequest.class)))
                .thenReturn(responseWithContent("{\"suggestions\":{\"a\":1}}"));

        List<ReorganizationSuggestionDTO> result = adapter.generateDailyPlanSuggestions("10", "context",
                LocalDate.of(2026, 5, 20));

        assertTrue(result.isEmpty());
    }

    @Test
    void generateDailyPlanSuggestions_withMalformedJson_returnsEmptyList() {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        when(groqAIClient.chatCompletion(eq("Bearer test-key"), any(GroqRequest.class)))
                .thenReturn(responseWithContent("{bad-json"));

        List<ReorganizationSuggestionDTO> result = adapter.generateDailyPlanSuggestions("10", "context",
                LocalDate.of(2026, 5, 20));

        assertTrue(result.isEmpty());
    }

    @Test
    void fallbackDailyPlanToMistral_delegatesToMistralAdapter() throws Exception {
        GroqAIClient groqAIClient = mock(GroqAIClient.class);
        MistralAdapter mistralAdapter = mock(MistralAdapter.class);
        GroqAdapter adapter = new GroqAdapter(groqAIClient, "test-key", "llama-test", new ObjectMapper(),
                mistralAdapter);

        LocalDate date = LocalDate.of(2026, 5, 20);
        List<ReorganizationSuggestionDTO> expected = List.of(ReorganizationSuggestionDTO.builder()
                .taskId("x")
                .title("t")
                .fromDay(date)
                .toDay(date.plusDays(1))
                .justification("ok")
                .build());
        when(mistralAdapter.generateDailyPlanSuggestions("10", "ctx", date)).thenReturn(expected);

        Method method = GroqAdapter.class.getDeclaredMethod(
                "fallbackDailyPlanToMistral", String.class, String.class, LocalDate.class, Throwable.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<ReorganizationSuggestionDTO> result = (List<ReorganizationSuggestionDTO>) method.invoke(
                adapter, "10", "ctx", date, new RuntimeException("groq down"));

        assertFalse(result.isEmpty());
        assertEquals("x", result.get(0).getTaskId());
        verify(mistralAdapter).generateDailyPlanSuggestions("10", "ctx", date);
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
