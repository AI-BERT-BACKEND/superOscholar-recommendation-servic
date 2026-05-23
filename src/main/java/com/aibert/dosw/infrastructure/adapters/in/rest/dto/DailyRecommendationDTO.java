package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * - message ("Aquí tienes tus recomendaciones" / "Aún no hay suficientes
 * datos")
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRecommendationDTO {
    @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String studentId;
    private List<RecommendationItemDTO> recommendations; // R18: Array de 1-5 recomendaciones
    @Schema(example = "CARGA") // R18: PRODUCTIVIDAD, CARGA, GENERAL
    private String recommendationType;
    @Schema(example = "0.87") // R18: Score global combinado (0.0-1.0)
    private Double confidenceScore;
    @Schema(example = "Aquí tienes tus recomendaciones") // R18: Mensaje descriptivo obligatorio
    private String message;
    @Schema(example = "¡Vas muy bien! Mantén el ritmo y llegarás a la meta.")
    private String motivationalMessage;
    @Schema(example = "2026-05-22")
    private LocalDate dateGenerated;
}
