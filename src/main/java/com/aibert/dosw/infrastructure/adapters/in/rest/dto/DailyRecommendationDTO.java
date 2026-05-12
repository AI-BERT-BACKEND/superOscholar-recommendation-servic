package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * R18: Respuesta del sistema de recomendaciones personalizadas.
 *
 * Campos obligatorios según spec:
 * - recommendations (Array 1-5)
 * - recommendationType (PRODUCTIVIDAD / CARGA / GENERAL)
 * - confidenceScore (0.0 - 1.0)
 * - message ("Aquí tienes tus recomendaciones" / "Aún no hay suficientes datos")
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRecommendationDTO {
    private Long studentId;
    private List<RecommendationItemDTO> recommendations; // R18: Array de 1-5 recomendaciones
    private String recommendationType;                    // R18: PRODUCTIVIDAD, CARGA, GENERAL
    private Double confidenceScore;                       // R18: Score global combinado (0.0-1.0)
    private String message;                               // R18: Mensaje descriptivo obligatorio
    private String motivationalMessage;                   // Mensaje motivacional de la IA
    private LocalDate dateGenerated;
}
