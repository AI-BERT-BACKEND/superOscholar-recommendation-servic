package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import com.aibert.dosw.domain.model.TaskDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * R19: Respuesta de sugerencias diarias — qué estudiar hoy y qué reprogramar.
 *
 * Campos obligatorios según spec:
 * - todayTasks (Array, máx 5, ordenadas por prioridad)
 * - reschedulableTasks (Array, deadline > 3 días + prioridad LOW/MEDIUM)
 * - reorganizationSuggestions (Array, solo cuando hay reprogramables) — Nuevo R19
 * - urgentAlert (Boolean, true si deadline < 24h)
 * - message (String, mensaje descriptivo)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanDTO {
    private Long studentId;
    private LocalDate planDate;
    private List<TaskDTO> todayTasks;                                       // R19: Antes "suggestedTasks", renombrado
    private List<TaskDTO> reschedulableTasks;                               // R19: Tareas reprogramables
    private List<ReorganizationSuggestionDTO> reorganizationSuggestions;    // R19-RN-04: Sugerencias de reorganización
    private Integer totalEstimatedMinutes;
    private boolean urgentAlert;                                            // R19: true si deadline < 24h
    private String message;                                                 // R19: Mensaje descriptivo obligatorio
}
