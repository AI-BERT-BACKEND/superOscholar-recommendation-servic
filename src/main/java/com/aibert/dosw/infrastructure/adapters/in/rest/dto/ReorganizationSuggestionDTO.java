package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * AIB-29: Sugerencia de reorganización semanal generada por la IA.
 * Solo se genera cuando hay tareas reprogramables disponibles
 * (deadline > 3 días y prioridad LOW o MEDIUM).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorganizationSuggestionDTO {
    @Schema(example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private String taskId;
    @Schema(example = "Lectura de Ingeniería de Software")
    private String title; // Título de la tarea para visualización
    @Schema(example = "2026-05-22")
    private LocalDate fromDay; // Día actual de la tarea en el plan
    @Schema(example = "2026-05-24")
    private LocalDate toDay; // Día sugerido al que mover la tarea
    @Schema(example = "El jueves tienes más disponibilidad y la tarea tiene deadline el 27.")
    private String justification; // Motivo de la sugerencia (máx. 300 chars)
}
