package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TaskFeignClientAdapter implements TaskServicePort {

    @Override
    public List<TaskDTO> getPrioritizedTasks(Long studentId) {
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
