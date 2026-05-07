package com.aibert.dosw.infrastructure.adapters.in.rest;

import com.aibert.dosw.application.dto.request.RecommendationRequest;
import com.aibert.dosw.domain.port.in.GenerateRecommendationUseCase;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final GenerateRecommendationUseCase generateRecommendationUseCase;
    private final GenerateDailyPlanUseCase generateDailyPlanUseCase;

    /**
     * R18 — POST /recommendations
     * Genera recomendaciones personalizadas (lista 1–5) usando Gemini + datos internos.
     */
    @PostMapping
    public ResponseEntity<DailyRecommendationDTO> generateRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        return ResponseEntity.ok(generateRecommendationUseCase.execute(request.getStudentId()));
    }

    /**
     * R19 — GET /recommendations/daily/{studentId}
     * Genera sugerencias diarias: todayTasks y reschedulableTasks.
     */
    @GetMapping("/daily/{studentId}")
    public ResponseEntity<DailyPlanDTO> getDailyPlan(@PathVariable Long studentId) {
        return ResponseEntity.ok(generateDailyPlanUseCase.execute(studentId));
    }
}
