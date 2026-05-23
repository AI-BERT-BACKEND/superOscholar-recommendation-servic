package com.aibert.dosw.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Contexto enriquecido que el engineplaning-service envía junto al request.
 * Contiene los datos que el planning-engine ya calculó para evitar
 * llamadas circulares entre microservicios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationContextDTO {

    @Min(value = 0, message = "pendingTaskCount no puede ser negativo")
    @Schema(example = "8")
    private Integer pendingTaskCount;

    @Min(value = 0, message = "criticalTaskCount no puede ser negativo")
    @Schema(example = "2")
    private Integer criticalTaskCount;

    @Min(value = 0, message = "unassignedTaskCount no puede ser negativo")
    @Schema(example = "3")
    private Integer unassignedTaskCount;

    @Schema(example = "false")
    private Boolean fullyAssigned;

    @Min(value = 1, message = "dailyCapMinutes debe ser al menos 1")
    @Max(value = 1440, message = "dailyCapMinutes no puede superar 1440 (24h)")
    @Schema(example = "480")
    private Integer dailyCapMinutes;

    @Valid
    private List<OverloadedDay> overloadedDays;

    @Valid
    private List<TaskContext> tasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverloadedDay {
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(example = "2026-05-22")
        private LocalDate date;

        @Min(value = 0, message = "excessMinutes no puede ser negativo")
        @Schema(example = "45")
        private Integer excessMinutes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskContext {
        @Schema(example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
        private String id;

        @Schema(example = "Parcial de Estructuras de Datos")
        private String title;

        @Min(value = 1, message = "estimatedDurationMinutes debe ser al menos 1")
        @Schema(example = "90")
        private Integer estimatedDurationMinutes;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @Schema(example = "2026-05-23T23:59:00")
        private LocalDateTime deadline;

        @Schema(example = "HIGH") // CRITICAL | HIGH | MEDIUM | LOW
        private String priority;

        @Schema(example = "TODO") // TODO | IN_PROGRESS | SCHEDULED
        private String status;

        @Schema(example = "EXAMEN") // TAREA | EXAMEN | PROYECTO | LECTURA | OTRO
        private String type;

        @Min(value = 1, message = "difficulty debe estar entre 1 y 5")
        @Max(value = 5, message = "difficulty debe estar entre 1 y 5")
        @Schema(example = "4")
        private Integer difficulty;

        @Schema(example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        private String subjectId;
    }
}
