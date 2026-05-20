package com.aibert.dosw.infrastructure.adapters.out.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tarea candidata a crítica que se envía al engineplanning-service.
 * Coincide exactamente con el contrato CriticalTaskCandidateRequest del planning-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriticalTaskCandidateRequest {
    private String taskId;
    private String title;
    private String subjectId;
    private String taskType;
    private LocalDateTime deadline;
    private LocalDateTime scheduledDate;
    private Integer estimatedDurationMinutes;
    private Double priorityScore;
    private String priorityLevel;
    private String status;
}
