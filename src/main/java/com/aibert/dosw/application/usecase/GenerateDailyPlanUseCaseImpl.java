package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateDailyPlanUseCaseImpl implements GenerateDailyPlanUseCase {

        private static final Logger log = LoggerFactory.getLogger(GenerateDailyPlanUseCaseImpl.class);

        private final TaskServicePort taskServicePort;
        private final GenerativeAiPort generativeAiPort;
        private static final int MAX_DAILY_TASKS = 5;
        private static final int MAX_REORGANIZATION_SUGGESTIONS = 5;

        @Override
        public DailyPlanDTO execute(String studentId, LocalDate currentDate) {
                // 1. Obtener todas las tareas priorizadas del estudiante
                List<TaskDTO> prioritizedTasks = taskServicePort.getPrioritizedTasks(studentId);

                if (prioritizedTasks == null) {
                        prioritizedTasks = new ArrayList<>();
                }

                // 2. RN-01: Máximo 5 tareas para evitar sobrecarga cognitiva
                List<TaskDTO> todayTasks = prioritizedTasks.stream()
                                .limit(MAX_DAILY_TASKS)
                                .collect(Collectors.toList());

                // 3. RN-02: Tareas reprogramables — deadline > 3 días Y prioridad LOW/MEDIUM
                LocalDateTime thresholdDate = currentDate.plusDays(3).atStartOfDay();
                List<TaskDTO> reschedulableTasks = prioritizedTasks.stream()
                                .filter(task -> !todayTasks.contains(task))
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

                // 5. RN-03: Alerta urgente — true si alguna tarea vence en menos de 24h
                LocalDateTime within24h = currentDate.plusDays(1).atStartOfDay();
                boolean isUrgent = prioritizedTasks.stream()
                                .anyMatch(task -> task.getDeadline() != null
                                                && task.getDeadline().isBefore(within24h));

                // 6. AIB-29: Sugerencias de reorganización generadas por IA (Groq → Mistral)
                List<ReorganizationSuggestionDTO> suggestions = new ArrayList<>();
                if (!reschedulableTasks.isEmpty()) {
                        String enrichedContext = buildContext(reschedulableTasks, currentDate);
                        try {
                                List<ReorganizationSuggestionDTO> aiSuggestions = generativeAiPort
                                                .generateDailyPlanSuggestions(
                                                                studentId, enrichedContext, currentDate);
                                suggestions = aiSuggestions.stream()
                                                .limit(MAX_REORGANIZATION_SUGGESTIONS)
                                                .collect(Collectors.toList());
                                log.info("IA generó {} sugerencia(s) de plan diario para studentId={}",
                                                suggestions.size(), studentId);
                        } catch (Exception e) {
                                log.warn("IA no disponible para plan diario de studentId={}, usando fallback determinista: {}",
                                                studentId, e.getMessage());
                                suggestions = buildDeterministicSuggestions(reschedulableTasks, currentDate);
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

        /**
         * Construye el contexto enriquecido con las tareas reprogramables
         * para que la IA genere sugerencias personalizadas (RN-01 AIB-29).
         */
        private String buildContext(List<TaskDTO> reschedulableTasks, LocalDate currentDate) {
                StringBuilder ctx = new StringBuilder();
                ctx.append("Fecha actual: ").append(currentDate).append("\n");
                ctx.append("Tareas que pueden reprogramarse (prioridad baja/media, deadline > 3 días):\n");
                for (TaskDTO task : reschedulableTasks) {
                        ctx.append("- ID: ").append(task.getTaskId())
                                        .append(", Título: ").append(task.getTitle())
                                        .append(", Deadline: ").append(task.getDeadline())
                                        .append(", Prioridad: ").append(task.getPriorityLevel())
                                        .append(", Duración estimada: ")
                                        .append(task.getEstimatedDurationMinutes() != null
                                                        ? task.getEstimatedDurationMinutes() + " min"
                                                        : "N/A")
                                        .append("\n");
                }
                return ctx.toString();
        }

        /**
         * Fallback determinista cuando la IA no está disponible.
         * Sugiere mover la tarea al menos 2 días adelante pero antes de su deadline.
         */
        private List<ReorganizationSuggestionDTO> buildDeterministicSuggestions(
                        List<TaskDTO> reschedulableTasks, LocalDate currentDate) {
                return reschedulableTasks.stream()
                                .limit(MAX_REORGANIZATION_SUGGESTIONS)
                                .map(task -> {
                                        LocalDate proposed = currentDate.plusDays(2);
                                        LocalDate deadlineDate = task.getDeadline().toLocalDate();
                                        if (!proposed.isBefore(deadlineDate)) {
                                                proposed = deadlineDate.minusDays(1).isAfter(currentDate)
                                                                ? deadlineDate.minusDays(1)
                                                                : currentDate.plusDays(1);
                                        }
                                        return ReorganizationSuggestionDTO.builder()
                                                        .taskId(task.getTaskId())
                                                        .title(task.getTitle())
                                                        .fromDay(currentDate)
                                                        .toDay(proposed)
                                                        .justification(
                                                                        "Esta tarea tiene baja urgencia y prioridad, "
                                                                                        + "puedes abordarla más adelante para reducir tu carga de hoy.")
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }
}
