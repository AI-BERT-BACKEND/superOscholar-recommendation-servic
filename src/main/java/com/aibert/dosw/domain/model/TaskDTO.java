package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String subject;
    private Double priorityScore; 
    private String priorityLevel; // ALTA, MEDIA, BAJA
    private LocalDate deadline;
    private Integer estimatedDurationMinutes;
}
