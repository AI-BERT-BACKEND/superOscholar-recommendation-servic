package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.CriticalRecommendationsResult;
import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsRequest;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.CriticalRecommendationsResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.PrioritizedTaskResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnginePlanningAdapterTest {

    @Mock
    private EnginePlanningClient enginePlanningClient;

    @InjectMocks
    private EnginePlanningAdapter adapter;

    // ── Mode A: null orderedTasks ──────────────────────────────────────────────

    @Test
    void getCriticalRecommendations_modeA_sendsNullCandidates() {
        ApiResponse<CriticalRecommendationsResponse> response = successResponse(List.of(), 0, "ok");
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        adapter.getCriticalRecommendations("s1", null);

        ArgumentCaptor<CriticalRecommendationsRequest> captor =
                ArgumentCaptor.forClass(CriticalRecommendationsRequest.class);
        verify(enginePlanningClient).getCriticalRecommendations(eq("s1"), captor.capture());
        assertFalse(captor.getValue() == null, "Request body must be non-null");
        assertTrue(captor.getValue().getOrderedTasks() == null,
                "Mode A: orderedTasks must be null in request");
    }

    // ── Mode B: with orderedTasks ──────────────────────────────────────────────

    @Test
    void getCriticalRecommendations_modeB_sendsCandidateList() {
        List<TaskDTO> tasks = List.of(
                TaskDTO.builder().taskId("t1").title("Calc").subjectId("CALC-101")
                        .taskType("TAREA").deadline(LocalDateTime.now().plusDays(1))
                        .estimatedDurationMinutes(60).priorityScore(9.5).priorityLevel("CRITICAL")
                        .status("TODO").build(),
                TaskDTO.builder().taskId("t2").title("History").subjectId("HIST-201")
                        .taskType("EXAMEN").deadline(LocalDateTime.now().plusDays(3))
                        .estimatedDurationMinutes(45).priorityScore(8.0).priorityLevel("HIGH")
                        .status("TODO").build());

        ApiResponse<CriticalRecommendationsResponse> response = successResponse(List.of(), 0, "ok");
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        adapter.getCriticalRecommendations("s1", tasks);

        ArgumentCaptor<CriticalRecommendationsRequest> captor =
                ArgumentCaptor.forClass(CriticalRecommendationsRequest.class);
        verify(enginePlanningClient).getCriticalRecommendations(eq("s1"), captor.capture());
        assertNotNull(captor.getValue().getOrderedTasks());
        assertEquals(2, captor.getValue().getOrderedTasks().size());
        assertEquals("t1", captor.getValue().getOrderedTasks().get(0).getTaskId());
    }

    // ── Successful responses ───────────────────────────────────────────────────

    @Test
    void getCriticalRecommendations_successWithTasks_returnsMappedResult() {
        PrioritizedTaskResponse task = new PrioritizedTaskResponse(
                null, "task-1", "Algebra", "MATH-101", "TAREA",
                LocalDateTime.now().plusDays(2), null, 90, "TODO", 9, "CRITICAL", null, null);

        ApiResponse<CriticalRecommendationsResponse> response = successResponse(
                List.of(task), 1, "Tienes una tarea crítica");
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertTrue(result.isAvailableFromService());
        assertEquals(1, result.getCriticalCount());
        assertEquals("Tienes una tarea crítica", result.getMessage());
        assertEquals(1, result.getCriticalTasks().size());
        assertEquals("task-1", result.getCriticalTasks().get(0).getTaskId());
        assertEquals("Algebra", result.getCriticalTasks().get(0).getTitle());
        assertEquals("CRITICAL", result.getCriticalTasks().get(0).getPriorityLevel());
    }

    @Test
    void getCriticalRecommendations_successWithNullCriticalCount_usesListSize() {
        PrioritizedTaskResponse task = new PrioritizedTaskResponse(
                null, "t1", "Task", "S1", "TAREA",
                null, null, 30, "TODO", 7, "HIGH", null, null);

        CriticalRecommendationsResponse data =
                new CriticalRecommendationsResponse(List.of(task), null, "msg");
        ApiResponse<CriticalRecommendationsResponse> response =
                new ApiResponse<>(true, "ok", data);
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertEquals(1, result.getCriticalCount());
    }

    @Test
    void getCriticalRecommendations_successWithNullTaskList_returnsEmptyTasks() {
        CriticalRecommendationsResponse data =
                new CriticalRecommendationsResponse(null, 0, "sin tareas");
        ApiResponse<CriticalRecommendationsResponse> response =
                new ApiResponse<>(true, "ok", data);
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertTrue(result.getCriticalTasks().isEmpty());
    }

    // ── Fallback / error states ────────────────────────────────────────────────

    @Test
    void getCriticalRecommendations_nullApiResponse_returnsEmptyResult() {
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(null);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertFalse(result.isAvailableFromService());
        assertEquals(0, result.getCriticalCount());
        assertTrue(result.getCriticalTasks().isEmpty());
    }

    @Test
    void getCriticalRecommendations_notSuccessfulResponse_returnsEmptyResult() {
        ApiResponse<CriticalRecommendationsResponse> response =
                new ApiResponse<>(false, "service error", null);
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertFalse(result.isAvailableFromService());
        assertEquals(0, result.getCriticalCount());
    }

    @Test
    void getCriticalRecommendations_successButNullData_returnsEmptyResult() {
        ApiResponse<CriticalRecommendationsResponse> response =
                new ApiResponse<>(true, "ok", null);
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertFalse(result.isAvailableFromService());
        assertEquals(0, result.getCriticalCount());
    }

    @Test
    void getCriticalRecommendations_clientThrowsException_returnsEmptyResult() {
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any()))
                .thenThrow(new RuntimeException("engineplanning down"));

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertFalse(result.isAvailableFromService());
        assertEquals(0, result.getCriticalCount());
        assertTrue(result.getCriticalTasks().isEmpty());
        assertTrue(result.getMessage().contains("unavailable"));
    }

    // ── mapToDomain: field aliasing ────────────────────────────────────────────

    @Test
    void getCriticalRecommendations_taskIdNullUsesIdField() {
        PrioritizedTaskResponse task = new PrioritizedTaskResponse(
                "legacy-id", null, "Old Task", "S1", "TAREA",
                null, null, 30, "TODO", 6, "MEDIUM", null, null);

        ApiResponse<CriticalRecommendationsResponse> response = successResponse(List.of(task), 1, "ok");
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertEquals("legacy-id", result.getCriticalTasks().get(0).getTaskId());
    }

    @Test
    void getCriticalRecommendations_priorityLevelNullUsesPriorityField() {
        PrioritizedTaskResponse task = new PrioritizedTaskResponse(
                null, "t-alias", "Aliased Task", "S1", "PROYECTO",
                null, null, 45, "IN_PROGRESS", 8, null, "HIGH", null);

        ApiResponse<CriticalRecommendationsResponse> response = successResponse(List.of(task), 1, "ok");
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", null);

        assertEquals("HIGH", result.getCriticalTasks().get(0).getPriorityLevel());
    }

    @Test
    void getCriticalRecommendations_emptyOrderedTasks_sendsEmptyList() {
        ApiResponse<CriticalRecommendationsResponse> response =
                successResponse(Collections.emptyList(), 0, "none");
        when(enginePlanningClient.getCriticalRecommendations(eq("s1"), any())).thenReturn(response);

        CriticalRecommendationsResult result = adapter.getCriticalRecommendations("s1", Collections.emptyList());

        ArgumentCaptor<CriticalRecommendationsRequest> captor =
                ArgumentCaptor.forClass(CriticalRecommendationsRequest.class);
        verify(enginePlanningClient).getCriticalRecommendations(eq("s1"), captor.capture());
        assertNotNull(captor.getValue().getOrderedTasks());
        assertEquals(0, captor.getValue().getOrderedTasks().size());

        assertTrue(result.getCriticalTasks().isEmpty());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ApiResponse<CriticalRecommendationsResponse> successResponse(
            List<PrioritizedTaskResponse> tasks, int count, String message) {
        CriticalRecommendationsResponse data = new CriticalRecommendationsResponse(tasks, count, message);
        return new ApiResponse<>(true, message, data);
    }
}
