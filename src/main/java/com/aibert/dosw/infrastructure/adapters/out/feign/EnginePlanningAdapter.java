package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.CriticalRecommendationsResult;
import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.out.CriticalRecommendationsPort;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsRequest;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalTaskCandidateRequest;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.PrioritizedTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter que implementa {@link CriticalRecommendationsPort} consumiendo el
 * engineplanning-service mediante Feign.
 *
 * <p>
 * Soporta dos modos de llamada:
 * <ul>
 * <li><b>Modo A</b>: {@code orderedTasks = null} → el motor calcula las tareas
 * críticas.</li>
 * <li><b>Modo B</b>: {@code orderedTasks} con lista → se envía la lista
 * precalculada.</li>
 * </ul>
 */
@Component
public class EnginePlanningAdapter implements CriticalRecommendationsPort {

    private static final Logger log = LoggerFactory.getLogger(EnginePlanningAdapter.class);

    private final EnginePlanningClient enginePlanningClient;

    public EnginePlanningAdapter(EnginePlanningClient enginePlanningClient) {
        this.enginePlanningClient = enginePlanningClient;
    }

    @Override
    public CriticalRecommendationsResult getCriticalRecommendations(String studentId, List<TaskDTO> orderedTasks) {
        try {
            CriticalRecommendationsRequest requestBody = buildRequest(orderedTasks);

            log.info("Consultando engineplanning-service recomendaciones críticas studentId={} modo={}",
                    studentId, orderedTasks == null ? "AUTO" : "PRECALCULADO");

            ApiResponse<CriticalRecommendationsResponse> apiResponse = enginePlanningClient
                    .getCriticalRecommendations(studentId, requestBody);

            if (apiResponse == null || apiResponse.getData() == null) {
                log.warn("EnginePlanning respondió sin datos para studentId={} message={}",
                        studentId, apiResponse != null ? apiResponse.getMessage() : "null response");
                return emptyResult("Sin datos del planning-service", false);
            }

            CriticalRecommendationsResponse data = apiResponse.getData();
            List<TaskDTO> criticalTasks = mapToDomain(data.getCriticalRecommendations());

            log.info("EnginePlanning devolvió {} tarea(s) crítica(s) para studentId={}",
                    criticalTasks.size(), studentId);

            return new CriticalRecommendationsResult(
                    criticalTasks,
                    data.getCriticalCount() != null ? data.getCriticalCount() : criticalTasks.size(),
                    data.getMessage(),
                    true);
        } catch (Exception e) {
            log.warn("EnginePlanning no disponible para studentId={}. Error: {}", studentId, e.getMessage());
            return emptyResult("planning-service unavailable", false);
        }
    }

    // ── helpers ─────────────────────────────────────────────────────────────────

    private CriticalRecommendationsRequest buildRequest(List<TaskDTO> orderedTasks) {
        if (orderedTasks == null) {
            return new CriticalRecommendationsRequest(null); // Modo A
        }
        List<CriticalTaskCandidateRequest> candidates = orderedTasks.stream()
                .map(t -> new CriticalTaskCandidateRequest(
                        t.getTaskId(),
                        t.getTitle(),
                        t.getSubjectId(),
                        t.getTaskType(),
                        t.getDeadline(),
                        t.getScheduledDate(),
                        t.getEstimatedDurationMinutes(),
                        t.getPriorityScore(),
                        t.getPriorityLevel(),
                        t.getStatus()))
                .collect(Collectors.toList());
        return new CriticalRecommendationsRequest(candidates); // Modo B
    }

    private List<TaskDTO> mapToDomain(List<PrioritizedTaskResponse> responses) {
        if (responses == null)
            return Collections.emptyList();
        return responses.stream()
                .map(r -> TaskDTO.builder()
                        .taskId(r.getTaskId() != null ? r.getTaskId() : r.getId())
                        .title(r.getTitle())
                        .subjectId(r.getSubjectId())
                        .taskType(r.getTaskType())
                        .deadline(r.getDeadline())
                        .scheduledDate(r.getScheduledDate())
                        .estimatedDurationMinutes(r.getEstimatedDurationMinutes())
                        .status(r.getStatus())
                        .priorityScore((double) r.getPriorityScore())
                        .priorityLevel(r.getPriorityLevel() != null ? r.getPriorityLevel() : r.getPriority())
                        .build())
                .collect(Collectors.toList());
    }

    private CriticalRecommendationsResult emptyResult(String message, boolean available) {
        return new CriticalRecommendationsResult(Collections.emptyList(), 0, message, available);
    }
}
