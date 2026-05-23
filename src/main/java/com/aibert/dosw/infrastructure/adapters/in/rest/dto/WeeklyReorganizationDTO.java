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
 * AIB-30: Respuesta de sugerencias de reorganización semanal del estudio.
 *
 * Campos:
 * - reorganizationSuggestions: máximo 5 sugerencias de movimiento de tareas
 * entre días
 * - reschedulableTasks: tareas con deadline > 3 días y prioridad LOW o MEDIUM
 * - overloadedDays: días cuya carga supera el 80 % de la disponibilidad diaria
 * - message: "Aquí tienes sugerencias para reorganizar tu semana" o "Tu semana
 * está bien distribuida"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyReorganizationDTO {
    @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String studentId;
    @Schema(example = "2026-05-18")
    private LocalDate weekStartDate;
    private List<ReorganizationSuggestionDTO> reorganizationSuggestions; // AIB-30 RN-02: máx. 5
    private List<TaskDTO> reschedulableTasks; // AIB-30 RN-01
    private List<LocalDate> overloadedDays; // AIB-30 RN-03: >80 % carga
    @Schema(example = "Aquí tienes sugerencias para reorganizar tu semana")
    private String message;
}
