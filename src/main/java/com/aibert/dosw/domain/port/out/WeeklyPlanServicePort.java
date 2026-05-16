package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.WeeklyPlanBlock;

import java.time.LocalDate;
import java.util.List;

/**
 * AIB-30: Puerto de salida para obtener el plan semanal activo del estudiante
 * desde el planning-service (AIB-24).
 */
public interface WeeklyPlanServicePort {
    List<WeeklyPlanBlock> getWeeklyPlan(String studentId, LocalDate weekStart);
}
