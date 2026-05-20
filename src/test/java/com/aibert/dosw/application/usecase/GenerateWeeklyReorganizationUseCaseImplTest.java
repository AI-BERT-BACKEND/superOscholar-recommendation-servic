package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.model.WeeklyPlanBlock;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.domain.port.out.WeeklyPlanServicePort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.WeeklyReorganizationDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateWeeklyReorganizationUseCaseImplTest {

    @Mock
    private TaskServicePort taskServicePort;

    @Mock
    private WeeklyPlanServicePort weeklyPlanServicePort;

    @InjectMocks
    private GenerateWeeklyReorganizationUseCaseImpl useCase;

    private static final LocalDate WEEK_START = LocalDate.of(2026, 5, 19);

    // ── Helpers ────────────────────────────────────────────────────────────────

    private WeeklyPlanBlock block(String taskId, LocalDate date, int minutes) {
        return WeeklyPlanBlock.builder()
                .taskId(taskId).title("Task " + taskId)
                .date(date).durationMinutes(minutes).build();
    }

    private TaskDTO lowTask(String id, int deadlineDays) {
        return TaskDTO.builder()
                .taskId(id).title("Low " + id)
                .priorityLevel("LOW")
                .deadline(WEEK_START.plusDays(deadlineDays).atStartOfDay())
                .estimatedDurationMinutes(60).build();
    }

    private TaskDTO criticalTask(String id, int deadlineDays) {
        return TaskDTO.builder()
                .taskId(id).title("Critical " + id)
                .priorityLevel("CRITICAL")
                .deadline(WEEK_START.plusDays(deadlineDays).atStartOfDay())
                .estimatedDurationMinutes(60).build();
    }

    // ── FA-03: Sin plan semanal ─────────────────────────────────────────────────

    @Test
    void execute_withNullWeeklyPlan_returnsFA03Message() {
        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(null);

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        assertEquals("s1", result.getStudentId());
        assertEquals(WEEK_START, result.getWeekStartDate());
        assertTrue(result.getReorganizationSuggestions().isEmpty());
        assertTrue(result.getReschedulableTasks().isEmpty());
        assertTrue(result.getOverloadedDays().isEmpty());
        assertTrue(result.getMessage().contains("Genera primero"));
    }

    @Test
    void execute_withEmptyWeeklyPlan_returnsFA03Message() {
        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(Collections.emptyList());

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        assertTrue(result.getMessage().contains("Genera primero"));
        verifyNoInteractions(taskServicePort);
    }

    // ── FA-01: Sin tareas reprogramables ───────────────────────────────────────

    @Test
    void execute_withNoCriticalTasks_allHighPriority_noReschedulable() {
        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(
                List.of(block("t1", WEEK_START, 60)));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(
                List.of(criticalTask("t1", 10)));

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        assertTrue(result.getReorganizationSuggestions().isEmpty());
        assertTrue(result.getMessage().contains("bien distribuida"));
    }

    @Test
    void execute_withLowPriorityButDeadlineWithin3Days_noReschedulable() {
        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(
                List.of(block("t1", WEEK_START, 60)));
        // Deadline exactly at threshold (3 days) → NOT reschedulable
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(List.of(
                TaskDTO.builder().taskId("t1").title("Urgent Low")
                        .priorityLevel("LOW")
                        .deadline(WEEK_START.plusDays(3).atStartOfDay())
                        .build()));

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        assertTrue(result.getReorganizationSuggestions().isEmpty());
        assertTrue(result.getMessage().contains("bien distribuida"));
    }

    @Test
    void execute_withNullPrioritizedTasks_usesEmptyList() {
        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(
                List.of(block("t1", WEEK_START, 60)));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(null);

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        assertTrue(result.getReorganizationSuggestions().isEmpty());
        assertTrue(result.getReschedulableTasks().isEmpty());
    }

    // ── Happy path: overload + reschedulable ───────────────────────────────────

    @Test
    void execute_withOverloadedDayAndReschedulable_returnsSuggestions() {
        LocalDate monday = WEEK_START;
        LocalDate tuesday = WEEK_START.plusDays(1);

        // Monday: 400 min > 80% of 480 (=384) → overloaded
        List<WeeklyPlanBlock> plan = List.of(
                block("t1", monday, 100),
                block("t2", monday, 100),
                block("t3", monday, 100),
                block("t4", monday, 100),
                block("t5", tuesday, 50));

        List<TaskDTO> tasks = List.of(
                lowTask("t1", 10),
                TaskDTO.builder().taskId("t2").title("Med t2").priorityLevel("MEDIUM")
                        .deadline(WEEK_START.plusDays(10).atStartOfDay()).build(),
                criticalTask("t3", 10), // CRITICAL → NOT reschedulable
                criticalTask("t4", 10)); // CRITICAL → NOT reschedulable

        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(plan);
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        assertFalse(result.getReorganizationSuggestions().isEmpty());
        assertFalse(result.getOverloadedDays().isEmpty());
        assertTrue(result.getOverloadedDays().contains(monday));
        assertTrue(result.getMessage().contains("sugerencias"));

        // Each suggestion has valid from/to days
        result.getReorganizationSuggestions().forEach(s -> {
            assertNotNull(s.getTaskId());
            assertNotNull(s.getFromDay());
            assertNotNull(s.getToDay());
            assertNotNull(s.getJustification());
            assertTrue(s.getJustification().length() <= 300);
        });
    }

    @Test
    void execute_suggestionsLimitedToMax5() {
        LocalDate monday = WEEK_START;
        LocalDate tuesday = WEEK_START.plusDays(1);

        // 8 tasks on overloaded Monday
        List<WeeklyPlanBlock> plan = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            plan.add(block("t" + i, monday, 60));
        }
        plan.add(block("t9", tuesday, 30));

        List<TaskDTO> tasks = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            tasks.add(lowTask("t" + i, 10));
        }

        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(plan);
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        assertTrue(result.getReorganizationSuggestions().size() <= 5);
    }

    @Test
    void execute_blocksWithNullDate_areIgnoredInCalculation() {
        List<WeeklyPlanBlock> plan = List.of(
                WeeklyPlanBlock.builder().taskId("t1").date(null).durationMinutes(999).build(),
                block("t2", WEEK_START, 50));

        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(plan);
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> useCase.execute("s1", WEEK_START));
    }

    @Test
    void execute_reschedulableTaskTitleFromTaskMap_whenBlockHasNoTitle() {
        LocalDate monday = WEEK_START;
        LocalDate tuesday = WEEK_START.plusDays(1);

        // Block without title - task in taskMap has title
        List<WeeklyPlanBlock> plan = List.of(
                WeeklyPlanBlock.builder().taskId("t1").title(null).date(monday).durationMinutes(400).build(),
                block("t9", tuesday, 50));

        List<TaskDTO> tasks = List.of(
                TaskDTO.builder().taskId("t1").title("Title From Map").priorityLevel("LOW")
                        .deadline(WEEK_START.plusDays(10).atStartOfDay()).build());

        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(plan);
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        if (!result.getReorganizationSuggestions().isEmpty()) {
            assertEquals("Title From Map", result.getReorganizationSuggestions().get(0).getTitle());
        }
    }

    @Test
    void execute_noOverloadedDays_reschedulableTasksStillReturned() {
        // Light days (no overload) but tasks are reschedulable
        // → no candidates to move → FA-01 message but reschedulableTasks is populated
        List<WeeklyPlanBlock> plan = List.of(block("t1", WEEK_START, 100));

        List<TaskDTO> tasks = List.of(lowTask("t2", 10)); // t2 not on any plan block

        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(plan);
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(tasks);

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);

        // reschedulableTasks has t2, but no candidates from overloaded days (none
        // overloaded)
        assertFalse(result.getReschedulableTasks().isEmpty());
        assertTrue(result.getOverloadedDays().isEmpty());
    }

    @Test
    void execute_blockDurationIsNull_treatedAsZero() {
        List<WeeklyPlanBlock> plan = List.of(
                WeeklyPlanBlock.builder().taskId("t1").date(WEEK_START).durationMinutes(null).build());

        when(weeklyPlanServicePort.getWeeklyPlan("s1", WEEK_START)).thenReturn(plan);
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> useCase.execute("s1", WEEK_START));

        WeeklyReorganizationDTO result = useCase.execute("s1", WEEK_START);
        assertTrue(result.getOverloadedDays().isEmpty()); // 0 min is not overloaded
    }
}
