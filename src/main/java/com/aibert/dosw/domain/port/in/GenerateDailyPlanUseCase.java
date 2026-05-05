package com.aibert.dosw.domain.port.in;

import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;

public interface GenerateDailyPlanUseCase {
    DailyPlanDTO execute(Long studentId);
}
