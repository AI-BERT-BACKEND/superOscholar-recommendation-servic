package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.model.WeeklyPlanBlock;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Feign Client real hacia el planning-service.
 * - GET /planning/prioritization: tareas priorizadas (AIB-22).
 * - GET /planning/distribution: bloques del plan semanal activo
 * (AIB-24/AIB-30).
 * URL configurable vía ${services.planning.url}.
 */
@FeignClient(name = "planningServiceClient", url = "${services.planning.url}")
public interface PlanningFeignClient {

        @GetMapping("/planning/prioritization")
        List<TaskDTO> getPrioritizedTasks(@RequestParam("studentId") String studentId,
                        @RequestParam(value = "forceRecalculate", defaultValue = "false") boolean forceRecalculate);

        @GetMapping("/planning/distribution")
        List<WeeklyPlanBlock> getWeeklyPlan(@RequestParam("studentId") String studentId,
                        @RequestParam("weekStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart);
}
