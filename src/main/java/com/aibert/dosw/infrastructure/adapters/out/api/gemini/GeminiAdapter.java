package com.aibert.dosw.infrastructure.adapters.out.api.gemini;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.ExternalAIClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class GeminiAdapter implements GenerativeAiPort {

    private final ExternalAIClient externalAIClient;
    private final String geminiApiKey;
    private final ObjectMapper objectMapper;

    public GeminiAdapter(
            ExternalAIClient externalAIClient,
            @Value("${gemini.api.key}") String geminiApiKey,
            ObjectMapper objectMapper) {
        this.externalAIClient = externalAIClient;
        this.geminiApiKey = geminiApiKey;
        this.objectMapper = objectMapper;
    }

    @Override
    public Recommendation generateRecommendation(Long studentId, String enrichedContext) {
        String prompt = buildPrompt(enrichedContext);
        
        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        GeminiRequest.Content.builder()
                                .parts(List.of(GeminiRequest.Part.builder().text(prompt).build()))
                                .build()
                ))
                .build();

        GeminiResponse response = externalAIClient.generateContent(geminiApiKey, request);
        String generatedText = extractText(response);
        
        return parseAIResponse(studentId, generatedText);
    }

    private String buildPrompt(String enrichedContext) {
        return "Eres AI.BERT, un tutor inteligente y compasivo. " +
               "Analiza el siguiente contexto del estudiante:\n\n" +
               enrichedContext + "\n\n" +
               "Debes devolver tu respuesta ESTRICTAMENTE en el siguiente formato JSON puro (sin bloques de código markdown, solo el JSON):\n" +
               "{\n" +
               "  \"confidenceScore\": 0.95,\n" +
               "  \"motivationalMessage\": \"Tu mensaje motivacional aquí basado en el contexto.\",\n" +
               "  \"studyTips\": [\"Tip 1\", \"Tip 2\", \"Tip 3\"]\n" +
               "}";
    }

    private String extractText(GeminiResponse response) {
        if (response != null && response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            return response.getCandidates().get(0).getContent().getParts().get(0).getText();
        }
        return "{}";
    }

    private Recommendation parseAIResponse(Long studentId, String jsonText) {
        try {
            // Limpiar si la IA devuelve bloques markdown ```json ... ```
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
            // Fallback si la IA no devuelve un JSON válido
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
