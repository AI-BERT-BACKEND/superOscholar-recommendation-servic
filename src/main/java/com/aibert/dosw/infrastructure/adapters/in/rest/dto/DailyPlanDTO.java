package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import com.aibert.dosw.domain.model.TaskDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
 * - reorganizationSuggestions (Array, solo cuando hay reprogramables) — Nuevo
 * R19
 * - urgentAlert (Boolean, true si deadline < 24h)
 * - message (String, mensaje descriptivo)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanDTO {
    @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String studentId;
    @Schema(example = "2026-05-22")
    private LocalDate planDate;
    private List<TaskDTO> todayTasks; // R19: Antes "suggestedTasks", renombrado
    private List<TaskDTO> reschedulableTasks; // R19: Tareas reprogramables
    private List<ReorganizationSuggestionDTO> reorganization; // AIB-29: Sugerencias de reorganización semanal
    @Schema(example = "240")
    private Integer totalEstimatedMinutes;
    @Schema(example = "true") // R19: true si deadline < 24h
    private boolean urgentAlert;
    @Schema(example = "Tienes 1 tarea crítica para hoy. ¡Prioriza el parcial!") // R19: Mensaje descriptivo obligatorio
    private String message;
}
