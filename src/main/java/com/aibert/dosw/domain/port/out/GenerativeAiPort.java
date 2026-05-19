package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;

import java.time.LocalDate;
import java.util.List;

public interface GenerativeAiPort {
    /**
     * AIB-28: Genera recomendaciones personalizadas usando IA.
     *
     * @param studentId       ID del estudiante
     * @param enrichedContext Contexto enriquecido con datos reales del estudiante
     * @param requestType     Tipo de recomendación: PRODUCTIVIDAD, CARGA, GENERAL
     * @return Recommendation con tips, mensaje y score de la IA
     */
    Recommendation generateRecommendation(String studentId, String enrichedContext, String requestType);

    /**
     * AIB-29: Genera sugerencias de reorganización del plan diario usando IA.
     * Máximo 5 sugerencias con taskId, fromDay, toDay y justificación.
     *
     * @param studentId       ID del estudiante
     * @param enrichedContext Contexto con las tareas reprogramables y sus deadlines
     * @param currentDate     Fecha del plan diario
     * @return Lista de hasta 5 sugerencias de reorganización generadas por la IA
     */
    List<ReorganizationSuggestionDTO> generateDailyPlanSuggestions(
            String studentId, String enrichedContext, LocalDate currentDate);
}
