package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.WeeklyPlanBlock;
import com.aibert.dosw.domain.port.out.WeeklyPlanServicePort;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.DistributionPlanResponse;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ScheduledBlockResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AIB-30: Adaptador que obtiene el plan semanal activo del planning-service vía
 * Feign.
 * Retorna datos de fallback si el servicio no está disponible.
 */
@Component
public class WeeklyPlanFeignClientAdapter implements WeeklyPlanServicePort {

    private static final Logger log = LoggerFactory.getLogger(WeeklyPlanFeignClientAdapter.class);

    private final PlanningFeignClient planningFeignClient;

    public WeeklyPlanFeignClientAdapter(PlanningFeignClient planningFeignClient) {
        this.planningFeignClient = planningFeignClient;
    }

    @Override
    public List<WeeklyPlanBlock> getWeeklyPlan(String studentId, LocalDate weekStart) {
        try {
            log.info("Consultando planning-service para plan semanal del studentId={}, weekStart={}", studentId,
                    weekStart);
            ApiResponse<DistributionPlanResponse> response = planningFeignClient.getWeeklyPlan(studentId, weekStart);
            if (response == null || response.getData() == null) {
                log.warn("Planning-service respondió sin datos para plan semanal studentId={} message={}",
                        studentId, response != null ? response.getMessage() : "null response");
                return getFallbackWeeklyPlan(weekStart);
            }
            List<WeeklyPlanBlock> plan = mapAssignedBlocks(response.getData().getAssignedBlocks());
            log.info("Se obtuvieron {} bloques del plan semanal para studentId={}", plan.size(), studentId);
            return plan;
        } catch (Exception e) {
            log.warn("Planning-service no disponible para plan semanal studentId={}. Usando fallback. Error: {}",
                    studentId, e.getMessage());
            return getFallbackWeeklyPlan(weekStart);
        }
    }

    /**
     * Plan semanal de fallback para desarrollo / servicio no disponible.
     * weekStart = lunes: tareas HIGH generan día sobrecargado (420 min > 384 min
     * umbral 80 %).
     * plusDays(2): tareas LOW/MEDIUM reprogramables para demostrar AIB-30.
     */
    private List<WeeklyPlanBlock> getFallbackWeeklyPlan(LocalDate weekStart) {
        return List.of(
                WeeklyPlanBlock.builder().taskId("101").title("Estudiar Integrales")
                        .date(weekStart).durationMinutes(240).priority("HIGH").build(),
                WeeklyPlanBlock.builder().taskId("102").title("Leer Cap. 4")
                        .date(weekStart).durationMinutes(180).priority("HIGH").build(),
                WeeklyPlanBlock.builder().taskId("103").title("Laboratorio Física")
                        .date(weekStart.plusDays(1)).durationMinutes(90).priority("MEDIUM").build(),
                WeeklyPlanBlock.builder().taskId("104").title("Ensayo Ética")
                        .date(weekStart.plusDays(2)).durationMinutes(60).priority("LOW").build(),
                WeeklyPlanBlock.builder().taskId("105").title("Práctica Python")
                        .date(weekStart.plusDays(2)).durationMinutes(45).priority("LOW").build());
    }

    private List<WeeklyPlanBlock> mapAssignedBlocks(List<ScheduledBlockResponse> assignedBlocks) {
        if (assignedBlocks == null || assignedBlocks.isEmpty()) {
            return Collections.emptyList();
        }

        return assignedBlocks.stream()
                .map(this::toWeeklyPlanBlock)
                .collect(Collectors.toList());
    }

    private WeeklyPlanBlock toWeeklyPlanBlock(ScheduledBlockResponse block) {
        return WeeklyPlanBlock.builder()
                .taskId(block.getTaskId())
                .title(block.getTitle())
                .date(block.getScheduledDate() != null ? block.getScheduledDate().toLocalDate() : null)
                .durationMinutes(block.getEstimatedDurationMinutes())
                .priority(block.getPriority())
                .build();
    }
}
