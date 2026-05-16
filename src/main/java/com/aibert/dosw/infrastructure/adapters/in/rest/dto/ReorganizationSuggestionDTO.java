package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

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
    private String taskId;
    private String title; // Título de la tarea para visualización
    private LocalDate fromDay; // Día actual de la tarea en el plan
    private LocalDate toDay; // Día sugerido al que mover la tarea
    private String justification; // Motivo de la sugerencia (máx. 300 chars)
}
