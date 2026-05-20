package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateDailyPlanUseCaseImplTest {

    @Mock
    private TaskServicePort taskServicePort;

    @Mock
    private GenerativeAiPort generativeAiPort;

    @InjectMocks
    private GenerateDailyPlanUseCaseImpl useCase;

    private static final LocalDate TODAY = LocalDate.of(2026, 5, 19);

    // ── Helpers ────────────────────────────────────────────────────────────────

    private TaskDTO highTask(String id, int deadlineDays) {
        return TaskDTO.builder()
                .taskId(id).title("Task " + id)
                .priorityLevel("HIGH")
                .deadline(TODAY.plusDays(deadlineDays).atStartOfDay())
                .estimatedDurationMinutes(30)
                .build();
    }

    private TaskDTO lowTask(String id, int deadlineDays) {
        return TaskDTO.builder()
                .taskId(id).title("Low " + id)
                .priorityLevel("LOW")
                .deadline(TODAY.plusDays(deadlineDays).atStartOfDay())
                .estimatedDurationMinutes(45)
                .build();
    }

    private TaskDTO mediumTask(String id, int deadlineDays) {
        return TaskDTO.builder()
                .taskId(id).title("Medium " + id)
                .priorityLevel("MEDIUM")
                .deadline(TODAY.plusDays(deadlineDays).atStartOfDay())
                .estimatedDurationMinutes(60)
                .build();
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    void execute_withNoTasks_returnsEmptyPlanWithMessage() {
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertEquals("s1", result.getStudentId());
        assertEquals(TODAY, result.getPlanDate());
        assertTrue(result.getTodayTasks().isEmpty());
        assertTrue(result.getReschedulableTasks().isEmpty());
        assertTrue(result.getReorganization().isEmpty());
        assertEquals("No tienes tareas pendientes para hoy. ¡Buen trabajo!", result.getMessage());
        assertFalse(result.isUrgentAlert());
        assertEquals(0, result.getTotalEstimatedMinutes());
    }

    @Test
    void execute_withNullTasksFromService_handlesGracefully() {
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(null);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertTrue(result.getTodayTasks().isEmpty());
        assertEquals("No tienes tareas pendientes para hoy. ¡Buen trabajo!", result.getMessage());
        assertFalse(result.isUrgentAlert());
    }

    @Test
    void execute_withMoreThan5Tasks_limitsTo5() {
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertEquals(5, result.getTodayTasks().size());
    }

    @Test
    void execute_with5OrFewerTasks_takesAll() {
        List<TaskDTO> tasks = List.of(highTask("t1", 1), highTask("t2", 2));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertEquals(2, result.getTodayTasks().size());
        assertEquals("Aquí está tu plan para hoy", result.getMessage());
    }

    @Test
    void execute_reschedulableTaskCallsAIAndReturnsSuggestions() {
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        // 6th task: reschedulable (LOW + deadline > 3 days, not in todayTasks)
        tasks.add(lowTask("t6", 10));

        List<ReorganizationSuggestionDTO> aiSuggestions = List.of(
                ReorganizationSuggestionDTO.builder()
                        .taskId("t6").title("Low t6").toDay(TODAY.plusDays(3))
                        .justification("Can be moved").build());

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);
        when(generativeAiPort.generateDailyPlanSuggestions(eq("s1"), anyString(), eq(TODAY)))
                .thenReturn(aiSuggestions);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertEquals(5, result.getTodayTasks().size());
        assertEquals(1, result.getReschedulableTasks().size());
        assertEquals(1, result.getReorganization().size());
        verify(generativeAiPort).generateDailyPlanSuggestions(eq("s1"), anyString(), eq(TODAY));
    }

    @Test
    void execute_withMediumReschedulableTask_isIncluded() {
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        // MEDIUM + deadline > 3 days
        tasks.add(mediumTask("t6", 7));

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);
        when(generativeAiPort.generateDailyPlanSuggestions(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertEquals(1, result.getReschedulableTasks().size());
        assertEquals("t6", result.getReschedulableTasks().get(0).getTaskId());
    }

    @Test
    void execute_aiFailure_usesDeterministicFallback() {
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        tasks.add(lowTask("t6", 10));

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);
        when(generativeAiPort.generateDailyPlanSuggestions(any(), any(), any()))
                .thenThrow(new RuntimeException("AI unavailable"));

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertFalse(result.getReorganization().isEmpty());
        assertEquals("t6", result.getReorganization().get(0).getTaskId());
        assertNotNull(result.getReorganization().get(0).getJustification());
    }

    @Test
    void execute_aiReturnsTooManySuggestions_limitsTo5() {
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        tasks.add(lowTask("t6", 10));

        List<ReorganizationSuggestionDTO> manyAiSuggestions = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            manyAiSuggestions.add(ReorganizationSuggestionDTO.builder()
                    .taskId("t" + i).title("T" + i).toDay(TODAY.plusDays(3))
                    .justification("suggestion").build());
        }

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);
        when(generativeAiPort.generateDailyPlanSuggestions(any(), any(), any()))
                .thenReturn(manyAiSuggestions);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertTrue(result.getReorganization().size() <= 5);
    }

    @Test
    void execute_urgentTask_setsUrgentAlertTrue() {
        // Deadline within 24h: today at 18:00 < tomorrow at 00:00
        List<TaskDTO> tasks = List.of(
                TaskDTO.builder().taskId("t1").title("Urgent")
                        .priorityLevel("CRITICAL")
                        .deadline(TODAY.atTime(18, 0))
                        .estimatedDurationMinutes(60).build());

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertTrue(result.isUrgentAlert());
    }

    @Test
    void execute_farDeadline_setsUrgentAlertFalse() {
        List<TaskDTO> tasks = List.of(
                TaskDTO.builder().taskId("t1").title("Normal")
                        .priorityLevel("MEDIUM")
                        .deadline(TODAY.plusDays(5).atStartOfDay())
                        .estimatedDurationMinutes(60).build());

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertFalse(result.isUrgentAlert());
    }

    @Test
    void execute_onlyHighPriorityTasks_noReschedulable() {
        List<TaskDTO> tasks = List.of(
                TaskDTO.builder().taskId("t1").title("Critical")
                        .priorityLevel("CRITICAL")
                        .deadline(TODAY.plusDays(10).atStartOfDay())
                        .estimatedDurationMinutes(60).build());

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertTrue(result.getReschedulableTasks().isEmpty());
        assertTrue(result.getReorganization().isEmpty());
        verifyNoInteractions(generativeAiPort);
    }

    @Test
    void execute_totalMinutesCalculatedCorrectly() {
        List<TaskDTO> tasks = List.of(
                TaskDTO.builder().taskId("t1").title("T1").priorityLevel("HIGH")
                        .deadline(TODAY.plusDays(1).atStartOfDay()).estimatedDurationMinutes(60).build(),
                TaskDTO.builder().taskId("t2").title("T2").priorityLevel("HIGH")
                        .deadline(TODAY.plusDays(1).atStartOfDay()).estimatedDurationMinutes(45).build(),
                TaskDTO.builder().taskId("t3").title("T3 null").priorityLevel("HIGH")
                        .deadline(TODAY.plusDays(1).atStartOfDay()).estimatedDurationMinutes(null).build());

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertEquals(105, result.getTotalEstimatedMinutes());
    }

    @Test
    void execute_reschedulableTask_exactlyAtThreshold_isNotReschedulable() {
        // deadline = currentDate + 3 days → NOT strictly after threshold → NOT
        // reschedulable
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        tasks.add(TaskDTO.builder().taskId("t6").title("Boundary")
                .priorityLevel("LOW")
                .deadline(TODAY.plusDays(3).atStartOfDay()) // exactly 3 days = not after
                .estimatedDurationMinutes(30).build());

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertTrue(result.getReschedulableTasks().isEmpty());
        verifyNoInteractions(generativeAiPort);
    }

    @Test
    void execute_reschedulableTask_justAfterThreshold_isReschedulable() {
        // deadline = currentDate + 4 days → IS after threshold → IS reschedulable
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        tasks.add(lowTask("t6", 4)); // deadline = +4 days, strictly after threshold

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);
        when(generativeAiPort.generateDailyPlanSuggestions(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertEquals(1, result.getReschedulableTasks().size());
    }

    @Test
    void execute_deterministicFallback_proposedDateRespectDeadline() {
        // Task with deadline just 5 days out → deterministic proposed = +2
        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            tasks.add(highTask("t" + i, 1));
        }
        tasks.add(TaskDTO.builder().taskId("tResc").title("Reschedulable")
                .priorityLevel("LOW")
                .deadline(TODAY.plusDays(5).atStartOfDay())
                .estimatedDurationMinutes(30).build());

        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);
        when(generativeAiPort.generateDailyPlanSuggestions(any(), any(), any()))
                .thenThrow(new RuntimeException("AI down"));

        DailyPlanDTO result = useCase.execute("s1", TODAY);

        assertFalse(result.getReorganization().isEmpty());
        ReorganizationSuggestionDTO suggestion = result.getReorganization().get(0);
        // proposed date must be > today and < deadline
        assertTrue(suggestion.getToDay().isAfter(TODAY));
        assertTrue(suggestion.getToDay().isBefore(TODAY.plusDays(5)));
    }
}
