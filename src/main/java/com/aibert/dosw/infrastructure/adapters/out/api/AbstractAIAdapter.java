package com.aibert.dosw.infrastructure.adapters.out.api;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractAIAdapter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String RECOMMENDATIONS_FIELD = "recommendations";
    private static final String CONFIDENCE_SCORE_FIELD = "confidenceScore";
    private static final String MOTIVATIONAL_MESSAGE_FIELD = "motivationalMessage";
    protected static final String DEFAULT_MOTIVATION = "Tu puedes hacerlo.";

    protected final ObjectMapper objectMapper;

    protected AbstractAIAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected String buildPrompt(String enrichedContext, String requestType) {
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

    protected String buildDailyPlanPrompt(String enrichedContext, LocalDate currentDate) {
        return "Analiza las siguientes tareas reprogramables del estudiante para la fecha " + currentDate + ":\n\n" +
                enrichedContext + "\n\n" +
                "Genera hasta 5 sugerencias de reorganización indicando a qué día mover cada tarea. "
                + "Elige un toDay entre " + currentDate.plusDays(1) + " y " + currentDate.plusDays(7)
                + ". Justifica con datos reales del contexto.\n"
                + "Responde estrictamente en JSON puro:\n"
                + "{\n"
                + "  \"suggestions\": [\n"
                + "    {\n"
                + "      \"taskId\": \"id-de-la-tarea\",\n"
                + "      \"title\": \"Título de la tarea\",\n"
                + "      \"toDay\": \"YYYY-MM-DD\",\n"
                + "      \"justification\": \"Motivo concreto (máx 300 chars)\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
    }

    protected String extractText(GroqResponse response) {
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            GroqResponse.Message message = response.getChoices().get(0).getMessage();
            if (message != null && message.getContent() != null) {
                return message.getContent();
            }
        }
        return "{}";
    }

    protected Recommendation parseAIResponse(String studentId, String jsonText, String requestType) {
        try {
            String cleanJson = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);
            Double confidence = readDouble(root, CONFIDENCE_SCORE_FIELD, 0.0);
            String motivation = readText(root, MOTIVATIONAL_MESSAGE_FIELD, DEFAULT_MOTIVATION);
            List<Recommendation.RecommendationItem> items = parseRecommendationItems(root, requestType, confidence);
            return buildRecommendation(studentId, requestType, confidence, motivation, items);
        } catch (Exception e) {
            log.error("Error parseando respuesta de IA para studentId={}: {}", studentId, e.getMessage());
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
                                    .build()))
                    .dateGenerated(LocalDate.now())
                    .build();
        }
    }

    protected List<ReorganizationSuggestionDTO> parseDailyPlanSuggestions(
            String studentId, String jsonText, LocalDate currentDate) {
        try {
            String cleanJson = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleanJson);
            JsonNode suggestionsNode = root.get("suggestions");
            if (suggestionsNode == null || !suggestionsNode.isArray()) {
                return List.of();
            }
            List<ReorganizationSuggestionDTO> result = new ArrayList<>();
            for (JsonNode node : suggestionsNode) {
                String taskId = readText(node, "taskId", "");
                String title = readText(node, "title", "Tarea");
                String toDayStr = readText(node, "toDay", "");
                LocalDate toDay;
                try {
                    toDay = toDayStr.isBlank() ? currentDate.plusDays(2) : LocalDate.parse(toDayStr);
                    if (!toDay.isAfter(currentDate))
                        toDay = currentDate.plusDays(2);
                } catch (Exception ex) {
                    toDay = currentDate.plusDays(2);
                }
                String justification = readText(node, "justification", "");
                if (justification.length() > 300)
                    justification = justification.substring(0, 300);
                result.add(ReorganizationSuggestionDTO.builder()
                        .taskId(taskId)
                        .title(title)
                        .fromDay(currentDate)
                        .toDay(toDay)
                        .justification(justification)
                        .build());
            }
            return result.stream().limit(5).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error parseando sugerencias de plan diario para studentId={}: {}",
                    studentId, e.getMessage());
            return List.of();
        }
    }

    protected Recommendation buildRecommendation(
            String studentId,
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

    protected List<Recommendation.RecommendationItem> parseRecommendationItems(
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

    protected String readText(JsonNode root, String field, String defaultValue) {
        JsonNode node = root.get(field);
        return node != null ? node.asText() : defaultValue;
    }

    protected Double readDouble(JsonNode root, String field, Double defaultValue) {
        JsonNode node = root.get(field);
        return node != null ? node.asDouble() : defaultValue;
    }
}
