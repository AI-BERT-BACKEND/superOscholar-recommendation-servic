package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * AIB-30: Bloque de un plan semanal activo generado por AIB-24.
 * Representa una sesión de estudio asignada a un día específico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPlanBlock {
    private String taskId;
    private String title;
    private LocalDate date;
    private Integer durationMinutes;
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
}
