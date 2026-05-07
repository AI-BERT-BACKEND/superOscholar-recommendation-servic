package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.TaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign Client real hacia el planning-service para obtener tareas priorizadas.
 * URL configurable vía ${services.planning.url}.
 */
@FeignClient(
        name = "planningServiceClient",
        url = "${services.planning.url}"
)
public interface PlanningFeignClient {

    @GetMapping("/api/v1/planning/prioritize/{studentId}")
    List<TaskDTO> getPrioritizedTasks(@PathVariable("studentId") Long studentId);
}
