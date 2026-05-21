package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskFeignClientAdapterTest {

    @Mock
    private PlanningFeignClient planningFeignClient;

    @InjectMocks
    private TaskFeignClientAdapter adapter;

    @Test
    void getPrioritizedTasks_whenServiceAvailable_returnsTasksFromClient() {
        List<TaskDTO> expected = List.of(
                TaskDTO.builder().taskId("t1").title("Calculus").priorityLevel("HIGH").build(),
                TaskDTO.builder().taskId("t2").title("History").priorityLevel("MEDIUM").build());
        when(planningFeignClient.getPrioritizedTasks("s1", false))
                .thenReturn(new ApiResponse<>(true, "ok", expected));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s1");

        assertEquals(2, result.size());
        assertEquals("t1", result.get(0).getTaskId());
        verify(planningFeignClient).getPrioritizedTasks("s1", false);
    }

    @Test
    void getPrioritizedTasks_whenServiceThrows_returnsFallbackTasks() {
        when(planningFeignClient.getPrioritizedTasks("s1", false))
                .thenThrow(new RuntimeException("planning-service down"));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s1");

        assertEquals(6, result.size());
    }

    @Test
    void getPrioritizedTasks_fallback_hasExpectedPriorityLevels() {
        when(planningFeignClient.getPrioritizedTasks("s1", false))
                .thenThrow(new RuntimeException("down"));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s1");

        assertEquals("CRITICAL", result.get(0).getPriorityLevel());
        assertEquals("HIGH",     result.get(1).getPriorityLevel());
        assertEquals("MEDIUM",   result.get(2).getPriorityLevel());
        assertEquals("MEDIUM",   result.get(3).getPriorityLevel());
        assertEquals("MEDIUM",   result.get(4).getPriorityLevel());
        assertEquals("LOW",      result.get(5).getPriorityLevel());
    }

    @Test
    void getPrioritizedTasks_fallback_hasExpectedTaskIds() {
        when(planningFeignClient.getPrioritizedTasks("s1", false))
                .thenThrow(new RuntimeException("down"));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s1");

        assertEquals("101", result.get(0).getTaskId());
        assertEquals("102", result.get(1).getTaskId());
        assertEquals("106", result.get(5).getTaskId());
    }

    @Test
    void getPrioritizedTasks_fallback_hasDeadlinesInFuture() {
        when(planningFeignClient.getPrioritizedTasks("s1", false))
                .thenThrow(new RuntimeException("down"));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s1");

        LocalDate today = LocalDate.now();
        result.forEach(task -> {
            assertNotNull(task.getDeadline());
            assertFalse(task.getDeadline().toLocalDate().isBefore(today));
        });
    }

    @Test
    void getPrioritizedTasks_fallback_hasPositiveDurations() {
        when(planningFeignClient.getPrioritizedTasks("s1", false))
                .thenThrow(new RuntimeException("down"));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s1");

        result.forEach(task -> assertNotNull(task.getEstimatedDurationMinutes()));
        assertEquals(60,  result.get(0).getEstimatedDurationMinutes());
        assertEquals(45,  result.get(1).getEstimatedDurationMinutes());
        assertEquals(90,  result.get(2).getEstimatedDurationMinutes());
        assertEquals(120, result.get(3).getEstimatedDurationMinutes());
        assertEquals(60,  result.get(4).getEstimatedDurationMinutes());
        assertEquals(30,  result.get(5).getEstimatedDurationMinutes());
    }

    @Test
    void getPrioritizedTasks_whenEmptyListReturned_returnsEmptyList() {
        when(planningFeignClient.getPrioritizedTasks("s2", false))
                .thenReturn(new ApiResponse<>(true, "ok", List.of()));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s2");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getPrioritizedTasks_whenResponseWithoutData_returnsFallbackTasks() {
        when(planningFeignClient.getPrioritizedTasks("s1", false))
                .thenReturn(new ApiResponse<>(true, "ok", null));

        List<TaskDTO> result = adapter.getPrioritizedTasks("s1");

        assertEquals(6, result.size());
    }
}
