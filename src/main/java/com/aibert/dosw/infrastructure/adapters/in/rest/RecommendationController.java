package com.aibert.dosw.infrastructure.adapters.in.rest;

import com.aibert.dosw.domain.port.in.GenerateRecommendationUseCase;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final GenerateRecommendationUseCase generateRecommendationUseCase;
    private final GenerateDailyPlanUseCase generateDailyPlanUseCase;

    @GetMapping("/{studentId}/daily-recommendation")
    public ResponseEntity<DailyRecommendationDTO> getDailyRecommendation(@PathVariable Long studentId) {
        return ResponseEntity.ok(generateRecommendationUseCase.execute(studentId));
    }

    @GetMapping("/{studentId}/daily-plan")
    public ResponseEntity<DailyPlanDTO> getDailyPlan(@PathVariable Long studentId) {
        return ResponseEntity.ok(generateDailyPlanUseCase.execute(studentId));
    }
}
