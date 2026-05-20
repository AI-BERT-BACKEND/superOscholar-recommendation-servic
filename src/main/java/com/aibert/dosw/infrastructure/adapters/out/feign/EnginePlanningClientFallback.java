package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsRequest;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class EnginePlanningClientFallback implements EnginePlanningClient {

    private static final Logger log = LoggerFactory.getLogger(EnginePlanningClientFallback.class);

    @Override
    public ApiResponse<CriticalRecommendationsResponse> getCriticalRecommendations(
            String studentId,
            CriticalRecommendationsRequest request) {
        log.warn("EnginePlanning fallback activado para studentId={}", studentId);
        CriticalRecommendationsResponse empty = new CriticalRecommendationsResponse(
                Collections.emptyList(), 0, "planning-service unavailable");
        return new ApiResponse<>(false, "planning-service unavailable", empty);
    }
}
