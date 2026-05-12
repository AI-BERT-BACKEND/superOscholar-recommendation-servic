package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.Recommendation;

public interface GenerativeAiPort {
    /**
     * Genera recomendaciones personalizadas usando IA.
     *
     * @param studentId      ID del estudiante
     * @param enrichedContext Contexto enriquecido con datos reales del estudiante
     * @param requestType    Tipo de recomendación: PRODUCTIVIDAD, CARGA, GENERAL (nullable, default GENERAL)
     * @return Recommendation con tips, mensaje y score de la IA
     */
    Recommendation generateRecommendation(Long studentId, String enrichedContext, String requestType);
}
