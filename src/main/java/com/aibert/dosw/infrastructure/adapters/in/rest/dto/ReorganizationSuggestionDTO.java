package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * R19-RN-04: Sugerencia de reorganización semanal.
 * Solo se genera cuando hay tareas reprogramables disponibles
 * (deadline > 3 días y prioridad BAJA o MEDIA).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorganizationSuggestionDTO {
    private String taskId;
    private String taskTitle;
    private String suggestedDay;   // Nuevo día sugerido (ej: "2026-05-15")
    private String justification;  // Justificación de por qué se puede mover
}
