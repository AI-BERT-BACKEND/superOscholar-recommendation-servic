package com.aibert.dosw.domain.port.in;

import com.aibert.dosw.application.dto.request.RecommendationContextDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;

public interface GenerateRecommendationUseCase {
    /**
     * R18: Genera recomendaciones personalizadas.
     *
     * @param studentId   ID del estudiante (obligatorio)
     * @param requestType Tipo: PRODUCTIVIDAD, CARGA, GENERAL (opcional, default
     *                    GENERAL)
     * @param context     Contexto enriquecido del planning-service (opcional, puede
     *                    ser null)
     * @return DTO con array de recomendaciones, tipo, score y mensaje
     */
    DailyRecommendationDTO execute(String studentId, String requestType, RecommendationContextDTO context);
}
