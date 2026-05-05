package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateDailyPlanUseCaseImpl implements GenerateDailyPlanUseCase {

    private final TaskServicePort taskServicePort;
    private static final int MAX_DAILY_TASKS = 5;

    @Override
    public DailyPlanDTO execute(Long studentId) {
        // 1. Obtener todas las tareas priorizadas del estudiante
        List<TaskDTO> prioritizedTasks = taskServicePort.getPrioritizedTasks(studentId);

        // 2. Aplicar regla de negocio: Máximo 5 tareas para evitar burnout
        List<TaskDTO> suggestedTasks = prioritizedTasks.stream()
                .limit(MAX_DAILY_TASKS)
                .collect(Collectors.toList());

        // 3. Filtro de Reprogramación: Clasificar tareas en reschedulableTasks (deadline > 3 días Y prioridad Baja/Media)
        LocalDate thresholdDate = LocalDate.now().plusDays(3);
        List<TaskDTO> reschedulableTasks = prioritizedTasks.stream()
                .filter(task -> !suggestedTasks.contains(task)) // Clasificamos las que quedaron fuera del top 5
                .filter(task -> task.getDeadline() != null && task.getDeadline().isAfter(thresholdDate))
                .filter(task -> "BAJA".equalsIgnoreCase(task.getPriorityLevel()) || "MEDIA".equalsIgnoreCase(task.getPriorityLevel()))
                .collect(Collectors.toList());

        // 4. Calcular métricas auxiliares
        int totalMinutes = suggestedTasks.stream()
                .mapToInt(TaskDTO::getEstimatedDurationMinutes)
                .sum();

        // 5. Alerta Urgente: true si alguna tarea (de las sugeridas o todas) vence en menos de 24h (hoy o mañana)
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        boolean isUrgent = prioritizedTasks.stream()
                .anyMatch(task -> task.getDeadline() != null && !task.getDeadline().isAfter(tomorrow));

        // 6. Retornar el plan estructurado
        return DailyPlanDTO.builder()
                .studentId(studentId)
                .planDate(LocalDate.now())
                .suggestedTasks(suggestedTasks)
                .reschedulableTasks(reschedulableTasks)
                .totalEstimatedMinutes(totalMinutes)
                .urgentAlert(isUrgent)
                .build();
    }
}
