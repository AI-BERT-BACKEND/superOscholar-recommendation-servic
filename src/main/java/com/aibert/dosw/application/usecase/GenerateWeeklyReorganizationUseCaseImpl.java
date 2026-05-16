package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.model.WeeklyPlanBlock;
import com.aibert.dosw.domain.port.in.GenerateWeeklyReorganizationUseCase;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.domain.port.out.WeeklyPlanServicePort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.ReorganizationSuggestionDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.WeeklyReorganizationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AIB-30: Genera sugerencias de reorganización semanal del estudio.
 *
 * Reglas de negocio:
 * RN-01: Reprogramable = deadline > 3 días Y prioridad LOW o MEDIUM.
 * RN-02: Máximo 5 sugerencias por semana.
 * RN-03: Prioriza mover tareas desde los días más sobrecargados hacia los de
 * menor carga.
 * RN-04: El sistema nunca aplica sin confirmación explícita del estudiante.
 *
 * Flujos alternos:
 * FA-01: Sin tareas reprogramables → "Tu semana está bien distribuida..."
 * FA-03: Sin plan semanal activo → "Genera primero tu plan semanal..."
 */
@Service
@RequiredArgsConstructor
public class GenerateWeeklyReorganizationUseCaseImpl implements GenerateWeeklyReorganizationUseCase {

    private final TaskServicePort taskServicePort;
    private final WeeklyPlanServicePort weeklyPlanServicePort;

    private static final int MAX_DAILY_MINUTES = 480;
    private static final double OVERLOAD_THRESHOLD = 0.80;
    private static final int MAX_SUGGESTIONS = 5;
    private static final int RESCHEDULABLE_DAYS_THRESHOLD = 3;
    private static final int MAX_JUSTIFICATION_LENGTH = 300;

    @Override
    public WeeklyReorganizationDTO execute(String studentId, LocalDate currentDate) {
        // 1. Obtener plan semanal activo (AIB-24)
        List<WeeklyPlanBlock> weeklyPlan = weeklyPlanServicePort.getWeeklyPlan(studentId, currentDate);

        // FA-03: Sin plan semanal activo
        if (weeklyPlan == null || weeklyPlan.isEmpty()) {
            return WeeklyReorganizationDTO.builder()
                    .studentId(studentId)
                    .weekStartDate(currentDate)
                    .reorganizationSuggestions(Collections.emptyList())
                    .reschedulableTasks(Collections.emptyList())
                    .overloadedDays(Collections.emptyList())
                    .message("Genera primero tu plan semanal en la sección de distribución automática.")
                    .build();
        }

        // 2. Obtener tareas priorizadas para datos de deadline/prioridad (AIB-22)
        List<TaskDTO> prioritizedTasks = taskServicePort.getPrioritizedTasks(studentId);
        if (prioritizedTasks == null) {
            prioritizedTasks = Collections.emptyList();
        }
        Map<String, TaskDTO> taskMap = prioritizedTasks.stream()
                .collect(Collectors.toMap(TaskDTO::getTaskId, t -> t, (a, b) -> a));

        // 3. Calcular minutos totales por día
        Map<LocalDate, Integer> minutesPerDay = new HashMap<>();
        for (WeeklyPlanBlock block : weeklyPlan) {
            if (block.getDate() != null) {
                minutesPerDay.merge(
                        block.getDate(),
                        block.getDurationMinutes() != null ? block.getDurationMinutes() : 0,
                        Integer::sum);
            }
        }

        // 4. Identificar días sobrecargados (RN-03: >80 % de disponibilidad diaria)
        int overloadMinutes = (int) (MAX_DAILY_MINUTES * OVERLOAD_THRESHOLD);
        List<LocalDate> overloadedDays = minutesPerDay.entrySet().stream()
                .filter(e -> e.getValue() > overloadMinutes)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

        // 5. Identificar todas las tareas reprogramables (RN-01)
        LocalDate reschedulableThreshold = currentDate.plusDays(RESCHEDULABLE_DAYS_THRESHOLD);
        List<TaskDTO> reschedulableTasks = prioritizedTasks.stream()
                .filter(t -> t.getDeadline() != null
                        && t.getDeadline().toLocalDate().isAfter(reschedulableThreshold))
                .filter(t -> "LOW".equalsIgnoreCase(t.getPriorityLevel())
                        || "MEDIUM".equalsIgnoreCase(t.getPriorityLevel()))
                .collect(Collectors.toList());

        // FA-01: Sin tareas reprogramables
        if (reschedulableTasks.isEmpty()) {
            return WeeklyReorganizationDTO.builder()
                    .studentId(studentId)
                    .weekStartDate(currentDate)
                    .reorganizationSuggestions(Collections.emptyList())
                    .reschedulableTasks(Collections.emptyList())
                    .overloadedDays(overloadedDays)
                    .message("Tu semana está bien distribuida, no hay tareas para reorganizar.")
                    .build();
        }

        // 6. Candidatos: bloques de días sobrecargados que sean reprogramables (RN-03)
        Set<String> reschedulableIds = reschedulableTasks.stream()
                .map(TaskDTO::getTaskId)
                .collect(Collectors.toSet());

        List<WeeklyPlanBlock> candidates = weeklyPlan.stream()
                .filter(b -> overloadedDays.contains(b.getDate()))
                .filter(b -> reschedulableIds.contains(b.getTaskId()))
                .limit(MAX_SUGGESTIONS)
                .collect(Collectors.toList());

        // 7. Generar sugerencias con diaDestino = día de menor carga (RN-02, RN-03)
        List<ReorganizationSuggestionDTO> suggestions = new ArrayList<>();
        for (WeeklyPlanBlock block : candidates) {
            LocalDate toDay = findLeastLoadedDay(currentDate, minutesPerDay, overloadedDays, block.getDate());
            TaskDTO task = taskMap.get(block.getTaskId());
            String title = (task != null) ? task.getTitle()
                    : (block.getTitle() != null ? block.getTitle() : block.getTaskId());
            suggestions.add(ReorganizationSuggestionDTO.builder()
                    .taskId(block.getTaskId())
                    .title(title)
                    .fromDay(block.getDate())
                    .toDay(toDay)
                    .justification(buildJustification(block.getDate(), toDay, title))
                    .build());
        }

        // 8. Construir respuesta final
        String message = suggestions.isEmpty()
                ? "Tu semana está bien distribuida, no hay tareas para reorganizar."
                : "Aquí tienes sugerencias para reorganizar tu semana";

        return WeeklyReorganizationDTO.builder()
                .studentId(studentId)
                .weekStartDate(currentDate)
                .reorganizationSuggestions(suggestions)
                .reschedulableTasks(reschedulableTasks)
                .overloadedDays(overloadedDays)
                .message(message)
                .build();
    }

    /**
     * Encuentra el día de menor carga en la semana actual (currentDate + 6 días)
     * que no sea el día de origen ni un día sobrecargado.
     * Si no hay día disponible, retorna la semana siguiente.
     */
    private LocalDate findLeastLoadedDay(LocalDate currentDate,
            Map<LocalDate, Integer> minutesPerDay,
            List<LocalDate> overloadedDays,
            LocalDate fromDay) {
        return currentDate.datesUntil(currentDate.plusDays(7))
                .filter(d -> !d.equals(fromDay))
                .filter(d -> !overloadedDays.contains(d))
                .min(Comparator.comparingInt(d -> minutesPerDay.getOrDefault(d, 0)))
                .orElse(currentDate.plusDays(7));
    }

    private String buildJustification(LocalDate fromDay, LocalDate toDay, String title) {
        String raw = String.format(
                "La tarea \"%s\" puede moverse de %s a %s para reducir la sobrecarga del día de origen y optimizar tu disponibilidad semanal.",
                title, fromDay, toDay);
        return raw.length() > MAX_JUSTIFICATION_LENGTH ? raw.substring(0, MAX_JUSTIFICATION_LENGTH) : raw;
    }
}
