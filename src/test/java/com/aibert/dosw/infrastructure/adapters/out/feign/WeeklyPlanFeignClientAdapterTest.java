package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.WeeklyPlanBlock;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.DistributionPlanResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ScheduledBlockResponse;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeeklyPlanFeignClientAdapterTest {

    @Mock
    private PlanningFeignClient planningFeignClient;

    @InjectMocks
    private WeeklyPlanFeignClientAdapter adapter;

    private static final LocalDate WEEK_START = LocalDate.of(2026, 5, 18);

    @Test
    void getWeeklyPlan_whenServiceAvailable_returnsBlocksFromClient() {
        DistributionPlanResponse payload = new DistributionPlanResponse(
                "s1",
                List.of(
                        new ScheduledBlockResponse("t1", "Task 1", WEEK_START.atTime(10, 0), 90, "HIGH"),
                        new ScheduledBlockResponse("t2", "Task 2", WEEK_START.plusDays(1).atTime(14, 0), 60, "LOW")),
                "ok");
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenReturn(new ApiResponse<>(true, "ok", payload));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        assertEquals(2, result.size());
        assertEquals("t1", result.get(0).getTaskId());
        assertEquals(WEEK_START, result.get(0).getDate());
        assertEquals(90, result.get(0).getDurationMinutes());
        verify(planningFeignClient).getWeeklyPlan("s1", WEEK_START);
    }

    @Test
    void getWeeklyPlan_whenServiceThrows_returnsFallbackPlan() {
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenThrow(new RuntimeException("planning-service down"));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        assertEquals(5, result.size());
    }

    @Test
    void getWeeklyPlan_fallback_hasFirstTwoDaysOnWeekStart() {
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenThrow(new RuntimeException("down"));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        assertEquals(WEEK_START, result.get(0).getDate());
        assertEquals(WEEK_START, result.get(1).getDate());
        assertEquals(WEEK_START.plusDays(1), result.get(2).getDate());
        assertEquals(WEEK_START.plusDays(2), result.get(3).getDate());
        assertEquals(WEEK_START.plusDays(2), result.get(4).getDate());
    }

    @Test
    void getWeeklyPlan_fallback_mondayIsOverloaded() {
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenThrow(new RuntimeException("down"));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        int mondayMinutes = result.stream()
                .filter(b -> WEEK_START.equals(b.getDate()))
                .mapToInt(WeeklyPlanBlock::getDurationMinutes)
                .sum();
        assertEquals(420, mondayMinutes);
    }

    @Test
    void getWeeklyPlan_fallback_hasExpectedTaskIds() {
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenThrow(new RuntimeException("down"));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        assertEquals("101", result.get(0).getTaskId());
        assertEquals("102", result.get(1).getTaskId());
        assertEquals("103", result.get(2).getTaskId());
        assertEquals("104", result.get(3).getTaskId());
        assertEquals("105", result.get(4).getTaskId());
    }

    @Test
    void getWeeklyPlan_fallback_hasLowPriorityReschedulableBlocks() {
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenThrow(new RuntimeException("down"));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        long lowCount = result.stream()
                .filter(b -> "LOW".equals(b.getPriority()))
                .count();
        assertFalse(lowCount == 0, "Fallback plan should have at least one LOW-priority block");
    }

    @Test
    void getWeeklyPlan_whenEmptyListReturned_returnsEmptyList() {
        DistributionPlanResponse payload = new DistributionPlanResponse("s2", List.of(), "ok");
        when(planningFeignClient.getWeeklyPlan("s2", WEEK_START))
                .thenReturn(new ApiResponse<>(true, "ok", payload));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s2", WEEK_START);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getWeeklyPlan_fallback_weekStartIsPreservedInBlockDates() {
        LocalDate differentWeekStart = LocalDate.of(2026, 6, 1);
        when(planningFeignClient.getWeeklyPlan("s1", differentWeekStart))
                .thenThrow(new RuntimeException("down"));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", differentWeekStart);

        assertEquals(differentWeekStart, result.get(0).getDate());
        assertEquals(differentWeekStart.plusDays(2), result.get(4).getDate());
    }

    @Test
    void getWeeklyPlan_whenResponseWithoutData_returnsFallbackPlan() {
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenReturn(new ApiResponse<>(true, "ok", null));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        assertEquals(5, result.size());
    }

    @Test
    void getWeeklyPlan_whenScheduledDateIsNull_mapsNullDate() {
        DistributionPlanResponse payload = new DistributionPlanResponse(
                "s1",
                List.of(new ScheduledBlockResponse("t1", "Task 1", null, 30, "MEDIUM")),
                "ok");
        when(planningFeignClient.getWeeklyPlan("s1", WEEK_START))
                .thenReturn(new ApiResponse<>(true, "ok", payload));

        List<WeeklyPlanBlock> result = adapter.getWeeklyPlan("s1", WEEK_START);

        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getTaskId());
        assertNull(result.get(0).getDate());
        assertEquals(30, result.get(0).getDurationMinutes());
    }
}
