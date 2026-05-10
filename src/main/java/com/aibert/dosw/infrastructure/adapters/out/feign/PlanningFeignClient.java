package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign Client real hacia el planning-service para obtener tareas priorizadas.
 * Endpoint real: GET /planning/prioritization?studentId=...&forceRecalculate=false
 * URL configurable vía ${services.planning.url}.
 */
@FeignClient(
        name = "planningServiceClient",
        url = "${services.planning.url}"
)
public interface PlanningFeignClient {

    @GetMapping("/planning/prioritization")
    List<TaskDTO> getPrioritizedTasks(@RequestParam("studentId") String studentId,
                                      @RequestParam(value = "forceRecalculate", defaultValue = "false") boolean forceRecalculate);
}
