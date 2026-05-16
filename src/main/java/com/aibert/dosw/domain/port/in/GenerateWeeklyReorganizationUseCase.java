package com.aibert.dosw.domain.port.in;

import com.aibert.dosw.infrastructure.adapters.in.rest.dto.WeeklyReorganizationDTO;

import java.time.LocalDate;

/**
 * AIB-30: Puerto de entrada para generar sugerencias de reorganización semanal.
 */
public interface GenerateWeeklyReorganizationUseCase {
    WeeklyReorganizationDTO execute(String studentId, LocalDate currentDate);
}
