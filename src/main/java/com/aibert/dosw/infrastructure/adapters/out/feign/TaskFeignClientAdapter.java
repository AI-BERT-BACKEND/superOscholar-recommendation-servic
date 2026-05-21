package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.infrastructure.adapters.out.feign.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador que obtiene tareas priorizadas del planning-service real via Feign.
 * Si el servicio no esta disponible, retorna datos mock como fallback para desarrollo.
 */
@Component
public class TaskFeignClientAdapter implements TaskServicePort {

    private static final Logger log = LoggerFactory.getLogger(TaskFeignClientAdapter.class);

    private final PlanningFeignClient planningFeignClient;

    public TaskFeignClientAdapter(PlanningFeignClient planningFeignClient) {
        this.planningFeignClient = planningFeignClient;
    }

    @Override
    public List<TaskDTO> getPrioritizedTasks(String studentId) {
        try {
            log.info("Consultando planning-service para tareas priorizadas");
            ApiResponse<List<TaskDTO>> response = planningFeignClient.getPrioritizedTasks(studentId, false);
            if (response == null || response.getData() == null) {
                log.warn("Planning-service respondio sin datos de tareas priorizadas");
                return getFallbackTasks();
            }
            List<TaskDTO> tasks = response.getData();
            log.info("Se obtuvieron {} tareas priorizadas del planning-service", tasks.size());
            return tasks;
        } catch (Exception e) {
            log.warn("Planning-service no disponible para tareas priorizadas. Usando fallback. Error: {}",
                    e.getMessage());
            return getFallbackTasks();
        }
    }

    private List<TaskDTO> getFallbackTasks() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        return List.of(
                TaskDTO.builder().taskId("101").title("Estudiar Integrales").subjectId("CALC-101").priorityScore(9.8)
                        .priorityLevel("CRITICAL").deadline(today.plusDays(1)).estimatedDurationMinutes(60).build(),
                TaskDTO.builder().taskId("102").title("Leer Cap. 4").subjectId("HIST-201").priorityScore(8.5)
                        .priorityLevel("HIGH").deadline(today.plusDays(2)).estimatedDurationMinutes(45).build(),
                TaskDTO.builder().taskId("103").title("Laboratorio Fisica").subjectId("PHYS-101").priorityScore(8.0)
                        .priorityLevel("MEDIUM").deadline(today.plusDays(5)).estimatedDurationMinutes(90).build(),
                TaskDTO.builder().taskId("104").title("Ensayo Etica").subjectId("PHIL-301").priorityScore(7.5)
                        .priorityLevel("MEDIUM").deadline(today.plusDays(10)).estimatedDurationMinutes(120).build(),
                TaskDTO.builder().taskId("105").title("Ejercicios de Java").subjectId("PROG-201").priorityScore(7.0)
                        .priorityLevel("MEDIUM").deadline(today.plusDays(2)).estimatedDurationMinutes(60).build(),
                TaskDTO.builder().taskId("106").title("Repasar Vocabulario").subjectId("ENG-101").priorityScore(5.0)
                        .priorityLevel("LOW").deadline(today.plusDays(7)).estimatedDurationMinutes(30).build());
    }
}
