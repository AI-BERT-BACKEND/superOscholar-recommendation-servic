package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AIB-28: Cada recomendación individual dentro del array de recommendations.
 * Campos según spec: recommendation (texto), type (categoría), confidenceScore.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemDTO {
    @Schema(example = "Prioriza Cálculo diferencial")
    private String title; // Título corto (contexto adicional)
    @Schema(example = "Tu tarea de Cálculo tiene deadline en menos de 48h. Dedica al menos 90 minutos hoy para los ejercicios del capítulo 3.") // AIB-28
    private String recommendation;
    @Schema(example = "CARGA") // AIB-28: PRODUCTIVIDAD | CARGA | GENERAL
    private String type;
    @Schema(example = "0.87") // AIB-28: 0.0 - 1.0
    private Double confidenceScore;
}
