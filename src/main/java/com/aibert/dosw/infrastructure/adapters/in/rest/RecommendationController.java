package com.aibert.dosw.infrastructure.adapters.in.rest;

import com.aibert.dosw.application.dto.request.RecommendationRequest;
import com.aibert.dosw.domain.port.in.GenerateRecommendationUseCase;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations", description = "Recommendation and daily plan endpoints")
public class RecommendationController {

    private final GenerateRecommendationUseCase generateRecommendationUseCase;
    private final GenerateDailyPlanUseCase generateDailyPlanUseCase;

    /**
     * R18 — POST /recommendations
     * Generate personalized recommendations (1-5 items) using AI and internal data.
     */
    @PostMapping
    @Operation(summary = "Generate personalized recommendations", description = "Creates 1-5 recommendations using AI plus internal student data. "
            + "If requestType is missing or invalid, it falls back to GENERAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recommendations generated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailyRecommendationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "422", description = "Insufficient history", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content)
    })
    public ResponseEntity<DailyRecommendationDTO> generateRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        return ResponseEntity
                .ok(generateRecommendationUseCase.execute(request.getStudentId(), request.getRequestType()));
    }

    /**
     * R19 — GET /recommendations/daily/{studentId}
     * Generate a daily plan with today tasks, reschedulable tasks, and
     * reorganization suggestions.
     */
    @GetMapping("/daily/{studentId}")
    @Operation(summary = "Generate daily plan", description = "Builds a daily plan with up to 5 tasks, identifies reschedulable tasks, "
            + "and provides reorganization suggestions when applicable.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Daily plan generated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DailyPlanDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Unexpected error", content = @Content)
    })
    public ResponseEntity<DailyPlanDTO> getDailyPlan(
            @Parameter(description = "Student identifier") @PathVariable Long studentId,
            @Parameter(description = "Optional date for the plan (ISO format: YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate) {
        LocalDate effectiveDate = (currentDate != null) ? currentDate : LocalDate.now();
        return ResponseEntity.ok(generateDailyPlanUseCase.execute(studentId, effectiveDate));
    }
}
