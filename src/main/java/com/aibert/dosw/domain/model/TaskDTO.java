package com.aibert.dosw.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que mapea exactamente la respuesta del planning-service endpoint GET /planning/prioritization.
 *
 * Campos del contrato real:
 * - taskId (String): identificador único de la tarea
 * - title (String): nombre de la tarea
 * - subjectId (String): identificador de la materia
 * - deadline (LocalDateTime): fecha límite con formato ISO "2026-05-12T00:00:00"
 * - estimatedDurationMinutes (int): duración estimada en minutos
 * - priorityScore (double): puntaje de prioridad calculado por el engine
 * - priorityLevel (String): nivel de prioridad — "CRITICAL", "HIGH", "MEDIUM", "LOW"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private String taskId;
    private String title;
    private String subjectId;
    private Double priorityScore;
    private String priorityLevel; // CRITICAL, HIGH, MEDIUM, LOW
    private LocalDateTime deadline;
    private Integer estimatedDurationMinutes;
}
