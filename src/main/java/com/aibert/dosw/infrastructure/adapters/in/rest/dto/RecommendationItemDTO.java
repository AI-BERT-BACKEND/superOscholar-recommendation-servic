package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * R18: Cada recomendación individual dentro del array de recommendations.
 * Contiene su propio tipo, confianza, título y justificación detallada.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemDTO {
    private String title;
    private String description;
    private String recommendationType; // PRODUCTIVIDAD, CARGA, GENERAL
    private Double confidenceScore;    // 0.0 - 1.0
}
