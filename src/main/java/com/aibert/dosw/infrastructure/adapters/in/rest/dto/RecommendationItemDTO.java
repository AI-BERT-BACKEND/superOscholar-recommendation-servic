package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

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
    private String title; // Título corto (contexto adicional)
    private String recommendation; // AIB-28: Texto de la recomendación generada por IA
    private String type; // AIB-28: PRODUCTIVIDAD | CARGA | GENERAL
    private Double confidenceScore; // AIB-28: 0.0 - 1.0
}
