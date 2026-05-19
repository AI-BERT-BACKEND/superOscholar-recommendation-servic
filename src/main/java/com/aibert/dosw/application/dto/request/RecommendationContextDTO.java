package com.aibert.dosw.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Integer pendingTaskCount;

    @Min(value = 0, message = "criticalTaskCount no puede ser negativo")
    private Integer criticalTaskCount;

    @Min(value = 0, message = "unassignedTaskCount no puede ser negativo")
    private Integer unassignedTaskCount;

    private Boolean fullyAssigned;

    @Min(value = 1, message = "dailyCapMinutes debe ser al menos 1")
    @Max(value = 1440, message = "dailyCapMinutes no puede superar 1440 (24h)")
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
        private LocalDate date;

        @Min(value = 0, message = "excessMinutes no puede ser negativo")
        private Integer excessMinutes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskContext {
        private String id;
        private String title;

        @Min(value = 1, message = "estimatedDurationMinutes debe ser al menos 1")
        private Integer estimatedDurationMinutes;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime deadline;
        private String priority; // CRITICAL | HIGH | MEDIUM | LOW
        private String status; // TODO | IN_PROGRESS | SCHEDULED
        private String type; // TAREA | EXAMEN | PROYECTO | LECTURA | OTRO

        @Min(value = 1, message = "difficulty debe estar entre 1 y 5")
        @Max(value = 5, message = "difficulty debe estar entre 1 y 5")
        private Integer difficulty;

        private String subjectId;
    }
}
