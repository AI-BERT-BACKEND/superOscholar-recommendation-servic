package com.aibert.dosw.domain.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO que mapea la respuesta PrioritizedTaskResponse del planning-service
 * endpoint GET /planning/prioritization.
 *
 * Contrato real (planning-service):
 * - id / taskId : identificador único de la tarea (ambos nombres aceptados)
 * - title : nombre de la tarea
 * - subjectId : identificador de la materia
 * - taskType : tipo de tarea (TAREA, EXAMEN, PROYECTO…)
 * - deadline : fecha límite original ISO-8601
 * - scheduledDate: fecha planificada por el motor ISO-8601
 * - estimatedDurationMinutes: duración estimada (ya con factor AIB-22.4)
 * - status : estado de la tarea (TODO, IN_PROGRESS)
 * - priorityScore: puntaje 0-100 (40% deadline + 40% peso académico + 20%
 * duración)
 * - priorityLevel / priority: nivel LOW | MEDIUM | HIGH | CRITICAL (ambos
 * nombres aceptados)
 * - lastUpdated : timestamp de la última priorización
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    /** ID canónico — también se acepta el alias "id" del planning-service */
    @JsonAlias("id")
    @Schema(example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    private String taskId;

    @Schema(example = "Parcial de Estructuras de Datos")
    private String title;

    @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String subjectId;

    /** Tipo de tarea: TAREA, EXAMEN, PROYECTO, etc. */
    @Schema(example = "EXAMEN")
    private String taskType;

    /** Fecha límite original de la tarea */
    @Schema(example = "2026-05-23T23:59:00")
    private LocalDateTime deadline;

    /** Fecha planificada por el motor de distribución (AIB-24) */
    @Schema(example = "2026-05-22T09:00:00")
    private LocalDateTime scheduledDate;

    /**
     * Duración estimada en minutos (ya aplicado el factor de corrección AIB-22.4)
     */
    @Schema(example = "120")
    private Integer estimatedDurationMinutes;

    /** Estado de la tarea: TODO, IN_PROGRESS */
    @Schema(example = "TODO")
    private String status;

    /**
     * Puntaje 0-100: 40% proximidad deadline + 40% peso académico + 20% duración
     */
    @Schema(example = "92.5")
    private Double priorityScore;

    /**
     * Nivel de prioridad — también se acepta el alias "priority" del
     * planning-service
     */
    @JsonAlias("priority")
    @Schema(example = "CRITICAL")
    private String priorityLevel;

    /** Timestamp de la última priorización */
    @Schema(example = "2026-05-22T08:30:00")
    private LocalDateTime lastUpdated;
}
