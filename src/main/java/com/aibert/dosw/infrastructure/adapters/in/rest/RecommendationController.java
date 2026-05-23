package com.aibert.dosw.infrastructure.adapters.in.rest;

import com.aibert.dosw.application.dto.request.RecommendationRequest;
import com.aibert.dosw.domain.port.in.GenerateRecommendationUseCase;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.domain.port.in.GenerateWeeklyReorganizationUseCase;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.WeeklyReorganizationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@Tag(name = "Recommendations", description = "Manage AI-powered recommendations: generate personalised suggestions, daily plans, and weekly reorganisation proposals. (R18, R19, R20, AIB-29, AIB-30)")
public class RecommendationController {

        private final GenerateRecommendationUseCase generateRecommendationUseCase;
        private final GenerateDailyPlanUseCase generateDailyPlanUseCase;
        private final GenerateWeeklyReorganizationUseCase generateWeeklyReorganizationUseCase;

        /**
         * R18 — POST /recommendations
         * Generate personalized recommendations (1-5 items) using AI and internal data.
         */
        @PostMapping
        @Operation(summary = "Generate personalized recommendations", description = "Creates 1-5 recommendations using AI plus internal student data. "
                        + "If requestType is missing or invalid, it falls back to GENERAL.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Personalised recommendations generated successfully",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = DailyRecommendationDTO.class),
                                                        examples = @ExampleObject(name = "recommendations-generated", value = """
                                                                        {
                                                                          "studentId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                                          "recommendations": [
                                                                            {
                                                                              "title": "Prioriza Cálculo diferencial",
                                                                              "recommendation": "Tu tarea de Cálculo tiene deadline en menos de 48h. Dedica al menos 90 minutos hoy para los ejercicios del capítulo 3.",
                                                                              "type": "CARGA",
                                                                              "confidenceScore": 0.87
                                                                            }
                                                                          ],
                                                                          "recommendationType": "CARGA",
                                                                          "confidenceScore": 0.87,
                                                                          "message": "Aquí tienes tus recomendaciones",
                                                                          "motivationalMessage": "¡Vas muy bien! Mantén el ritmo y llegarás a la meta.",
                                                                          "dateGenerated": "2026-05-22"
                                                                        }
                                                                        """))),
                        @ApiResponse(responseCode = "400", description = "Validation error in request body", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token", content = @Content),
                        @ApiResponse(responseCode = "422", description = "Insufficient student history to generate recommendations", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Unexpected error generating recommendations", content = @Content)
        })
        public ResponseEntity<DailyRecommendationDTO> generateRecommendations(
                        @Valid @RequestBody RecommendationRequest request) {
                return ResponseEntity
                                .ok(generateRecommendationUseCase.execute(request.getStudentId(),
                                                request.getRequestType(), request.getContext()));
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
                        @ApiResponse(responseCode = "200", description = "Daily plan generated successfully",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = DailyPlanDTO.class),
                                                        examples = @ExampleObject(name = "daily-plan", value = """
                                                                        {
                                                                          "studentId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                                          "planDate": "2026-05-22",
                                                                          "todayTasks": [
                                                                            {
                                                                              "taskId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                                                              "title": "Parcial de Estructuras de Datos",
                                                                              "estimatedDurationMinutes": 120,
                                                                              "deadline": "2026-05-23T23:59:00",
                                                                              "priority": "CRITICAL",
                                                                              "status": "TODO",
                                                                              "taskType": "EXAMEN",
                                                                              "priorityScore": 92.5
                                                                            }
                                                                          ],
                                                                          "reschedulableTasks": [],
                                                                          "reorganization": [],
                                                                          "totalEstimatedMinutes": 120,
                                                                          "urgentAlert": true,
                                                                          "message": "Tienes 1 tarea crítica para hoy. ¡Prioriza el parcial!"
                                                                        }
                                                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid studentId or date format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Student not found or no active plan for the requested date", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Unexpected error generating daily plan", content = @Content)
        })
        public ResponseEntity<DailyPlanDTO> getDailyPlan(
                        @Parameter(description = "UUID of the student", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                        schema = @Schema(type = "string", format = "uuid")) @PathVariable String studentId,
                        @Parameter(description = "Optional date for the plan (ISO format: YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate) {
                LocalDate effectiveDate = (currentDate != null) ? currentDate : LocalDate.now();
                return ResponseEntity.ok(generateDailyPlanUseCase.execute(studentId, effectiveDate));
        }

        /**
         * AIB-30 — GET /recommendations/weekly/{studentId}
         * Generate weekly reorganization suggestions: overloaded days, reschedulable
         * tasks,
         * and concrete move proposals (max 5) ordered by overload priority.
         */
        @GetMapping("/weekly/{studentId}")
        @Operation(summary = "Generate weekly reorganization suggestions", description = "Analyzes the active weekly plan (AIB-24), identifies overloaded days (>80 % capacity) "
                        + "and reschedulable tasks (deadline > 3 days, LOW/MEDIUM priority), "
                        + "then proposes up to 5 concrete task moves. Never applied without explicit confirmation (RN-04).")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Weekly reorganisation suggestions generated successfully",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = WeeklyReorganizationDTO.class),
                                                        examples = @ExampleObject(name = "weekly-reorganization", value = """
                                                                        {
                                                                          "studentId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                                                          "weekStartDate": "2026-05-18",
                                                                          "reorganizationSuggestions": [
                                                                            {
                                                                              "taskId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
                                                                              "title": "Lectura de Ingeniería de Software",
                                                                              "fromDay": "2026-05-22",
                                                                              "toDay": "2026-05-24",
                                                                              "justification": "El jueves tienes más disponibilidad y la tarea tiene deadline el 27."
                                                                            }
                                                                          ],
                                                                          "reschedulableTasks": [],
                                                                          "overloadedDays": ["2026-05-22"],
                                                                          "message": "Aquí tienes sugerencias para reorganizar tu semana"
                                                                        }
                                                                        """))),
                        @ApiResponse(responseCode = "400", description = "Invalid studentId or date format", content = @Content),
                        @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token", content = @Content),
                        @ApiResponse(responseCode = "404", description = "Student not found or no active weekly plan for the requested date", content = @Content),
                        @ApiResponse(responseCode = "500", description = "Unexpected error generating weekly reorganisation", content = @Content)
        })
        public ResponseEntity<WeeklyReorganizationDTO> getWeeklyReorganization(
                        @Parameter(description = "UUID of the student", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                        schema = @Schema(type = "string", format = "uuid")) @PathVariable String studentId,
                        @Parameter(description = "Reference date for the week (ISO format: YYYY-MM-DD). Default: today.") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate currentDate) {
                LocalDate effectiveDate = (currentDate != null) ? currentDate : LocalDate.now();
                return ResponseEntity.ok(generateWeeklyReorganizationUseCase.execute(studentId, effectiveDate));
        }
}
