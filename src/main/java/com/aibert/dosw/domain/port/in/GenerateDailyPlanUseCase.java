package com.aibert.dosw.domain.port.in;

import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;

import java.time.LocalDate;

public interface GenerateDailyPlanUseCase {
    DailyPlanDTO execute(String studentId, LocalDate currentDate);
}
