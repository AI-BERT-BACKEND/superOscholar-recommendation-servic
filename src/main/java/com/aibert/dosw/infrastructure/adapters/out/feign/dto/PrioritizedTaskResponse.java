package com.aibert.dosw.infrastructure.adapters.out.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrioritizedTaskResponse {
    private String id;
    private String taskId;
    private String title;
    private String subjectId;
    private String taskType;
    private LocalDateTime deadline;
    private LocalDateTime scheduledDate;
    private int estimatedDurationMinutes;
    private String status;
    private int priorityScore;
    private String priorityLevel;
    private String priority;
    private LocalDateTime lastUpdated;
}
