package com.aibert.dosw.domain.port.in;

import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;

public interface GenerateRecommendationUseCase {
    /**
     * R18: Genera recomendaciones personalizadas.
     *
     * @param studentId   ID del estudiante (obligatorio)
     * @param requestType Tipo: PRODUCTIVIDAD, CARGA, GENERAL (opcional, default
     *                    GENERAL)
     * @return DTO con array de recomendaciones, tipo, score y mensaje
     */
    DailyRecommendationDTO execute(String studentId, String requestType);
}
