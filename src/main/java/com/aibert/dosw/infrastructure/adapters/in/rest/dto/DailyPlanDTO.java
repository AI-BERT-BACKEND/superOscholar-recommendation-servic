package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import com.aibert.dosw.domain.model.TaskDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailyPlanDTO {
    private Long studentId;
    private LocalDate planDate;
    private List<TaskDTO> suggestedTasks;
    private List<TaskDTO> reschedulableTasks;
    private Integer totalEstimatedMinutes;
    private boolean urgentAlert;
}
