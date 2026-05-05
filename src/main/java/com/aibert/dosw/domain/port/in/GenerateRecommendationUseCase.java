package com.aibert.dosw.domain.port.in;

import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;

public interface GenerateRecommendationUseCase {
    DailyRecommendationDTO execute(Long studentId);
}
