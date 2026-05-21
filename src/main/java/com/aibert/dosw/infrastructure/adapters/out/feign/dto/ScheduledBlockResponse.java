package com.aibert.dosw.infrastructure.adapters.out.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledBlockResponse {
    private String taskId;
    private String title;
    private LocalDateTime scheduledDate;
    private int estimatedDurationMinutes;
    private String priority;
}
