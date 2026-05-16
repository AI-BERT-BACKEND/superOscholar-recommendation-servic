package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateDailyPlanUseCaseImpl implements GenerateDailyPlanUseCase {

        private final TaskServicePort taskServicePort;
        private static final int MAX_DAILY_TASKS = 5;

        @Override
        public DailyPlanDTO execute(String studentId, LocalDate currentDate) {
                // 1. Obtener todas las tareas priorizadas del estudiante
                List<TaskDTO> prioritizedTasks = taskServicePort.getPrioritizedTasks(studentId);

                if (prioritizedTasks == null) {
                        prioritizedTasks = new ArrayList<>();
                }

                // 2. Aplicar regla de negocio: Máximo 5 tareas para evitar burnout
                List<TaskDTO> todayTasks = prioritizedTasks.stream()
                                .limit(MAX_DAILY_TASKS)
                                .collect(Collectors.toList());

                // 3. Filtro de Reprogramación: Clasificar tareas en reschedulableTasks
                // (deadline > 3 días Y prioridad LOW/MEDIUM — valores reales del
                // planning-service)
                LocalDateTime thresholdDate = currentDate.plusDays(3).atStartOfDay();
                List<TaskDTO> reschedulableTasks = prioritizedTasks.stream()
                                .filter(task -> !todayTasks.contains(task)) // Las que quedaron fuera del top 5
                                .filter(task -> task.getDeadline() != null && task.getDeadline().isAfter(thresholdDate))
                                .filter(task -> "LOW".equalsIgnoreCase(task.getPriorityLevel())
                                                || "MEDIUM".equalsIgnoreCase(task.getPriorityLevel()))
                                .collect(Collectors.toList());

                // 4. Calcular métricas auxiliares
                int totalMinutes = todayTasks.stream()
                                .mapToInt(task -> task.getEstimatedDurationMinutes() != null
                                                ? task.getEstimatedDurationMinutes()
                                                : 0)
                                .sum();

                // 5. Alerta Urgente (RN-03): true si alguna tarea vence en menos de 24h
                LocalDateTime within24h = currentDate.plusDays(1).atStartOfDay();
                boolean isUrgent = prioritizedTasks.stream()
                                .anyMatch(task -> task.getDeadline() != null
                                                && task.getDeadline().isBefore(within24h));

                // 6. RN-04: Sugerencias de reorganización solo si hay tareas reprogramables
                List<ReorganizationSuggestionDTO> suggestions = new ArrayList<>();
                if (!reschedulableTasks.isEmpty()) {
                        for (TaskDTO task : reschedulableTasks) {
                                suggestions.add(ReorganizationSuggestionDTO.builder()
                                                .taskId(task.getTaskId())
                                                .title(task.getTitle())
                                                .fromDay(currentDate)
                                                .toDay(task.getDeadline().minusDays(1).toLocalDate())
                                                .justification(
                                                                "Esta tarea tiene baja urgencia y prioridad, puedes abordarla más adelante para reducir tu carga de hoy.")
                                                .build());
                        }
                }

                // 7. Determinar el mensaje
                String message = todayTasks.isEmpty()
                                ? "No tienes tareas pendientes para hoy. ¡Buen trabajo!"
                                : "Aquí está tu plan para hoy";

                // 8. Retornar el plan estructurado
                return DailyPlanDTO.builder()
                                .studentId(studentId)
                                .planDate(currentDate)
                                .todayTasks(todayTasks)
                                .reschedulableTasks(reschedulableTasks)
                                .reorganization(suggestions)
                                .totalEstimatedMinutes(totalMinutes)
                                .urgentAlert(isUrgent)
                                .message(message)
                                .build();
        }
}
