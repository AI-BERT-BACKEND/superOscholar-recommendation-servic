package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsRequest;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "engineplanning-service", url = "${services.planning.url}", fallback = EnginePlanningClientFallback.class)
public interface EnginePlanningClient {

    @PostMapping("/planning/prioritization/critical")
    ApiResponse<CriticalRecommendationsResponse> getCriticalRecommendations(
            @RequestHeader("X-User-Id") String studentId,
            @RequestBody(required = false) CriticalRecommendationsRequest request);
}
