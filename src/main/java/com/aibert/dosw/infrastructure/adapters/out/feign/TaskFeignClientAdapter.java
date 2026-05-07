package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador que intenta obtener tareas priorizadas del planning-service real vía Feign.
 * Si el servicio no está disponible, retorna datos mock como fallback para desarrollo.
 */
@Component
public class TaskFeignClientAdapter implements TaskServicePort {

    private static final Logger log = LoggerFactory.getLogger(TaskFeignClientAdapter.class);

    private final PlanningFeignClient planningFeignClient;

    public TaskFeignClientAdapter(PlanningFeignClient planningFeignClient) {
        this.planningFeignClient = planningFeignClient;
    }

    @Override
    public List<TaskDTO> getPrioritizedTasks(Long studentId) {
        try {
            log.info("Consultando planning-service para tareas priorizadas del studentId={}", studentId);
            List<TaskDTO> tasks = planningFeignClient.getPrioritizedTasks(studentId);
            log.info("Se obtuvieron {} tareas priorizadas del planning-service", tasks.size());
            return tasks;
        } catch (Exception e) {
            log.warn("Planning-service no disponible para studentId={}. Usando datos de fallback. Error: {}",
                    studentId, e.getMessage());
            return getFallbackTasks();
        }
    }

    /**
     * Datos de fallback para cuando el planning-service no está disponible.
     * Garantiza que el recommendation-service siga funcionando de forma aislada.
     */
    private List<TaskDTO> getFallbackTasks() {
        LocalDate today = LocalDate.now();
        return List.of(
                TaskDTO.builder().id(101L).title("Estudiar Integrales").subject("Cálculo").priorityScore(9.8).priorityLevel("ALTA").deadline(today.plusDays(1)).estimatedDurationMinutes(60).build(),
                TaskDTO.builder().id(102L).title("Leer Cap. 4").subject("Historia").priorityScore(8.5).priorityLevel("ALTA").deadline(today.plusDays(2)).estimatedDurationMinutes(45).build(),
                TaskDTO.builder().id(103L).title("Laboratorio Física").subject("Física").priorityScore(8.0).priorityLevel("MEDIA").deadline(today.plusDays(5)).estimatedDurationMinutes(90).build(),
                TaskDTO.builder().id(104L).title("Ensayo Ética").subject("Filosofía").priorityScore(7.5).priorityLevel("MEDIA").deadline(today.plusDays(10)).estimatedDurationMinutes(120).build(),
                TaskDTO.builder().id(105L).title("Ejercicios de Java").subject("Programación").priorityScore(7.0).priorityLevel("MEDIA").deadline(today.plusDays(2)).estimatedDurationMinutes(60).build(),
                TaskDTO.builder().id(106L).title("Repasar Vocabulario").subject("Inglés").priorityScore(5.0).priorityLevel("BAJA").deadline(today.plusDays(7)).estimatedDurationMinutes(30).build()
        );
    }
}
