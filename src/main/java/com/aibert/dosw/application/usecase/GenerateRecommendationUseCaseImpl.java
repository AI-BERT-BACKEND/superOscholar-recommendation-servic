package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.mapper.RecommendationMapper;
import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.model.StudentActivityLog;
import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.in.GenerateRecommendationUseCase;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.domain.port.out.ProfileServicePort;
import com.aibert.dosw.domain.port.out.RecommendationRepository;
import com.aibert.dosw.domain.port.out.StudentActivityLogRepository;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.domain.exception.InsufficientHistoryException;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenerateRecommendationUseCaseImpl implements GenerateRecommendationUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateRecommendationUseCaseImpl.class);

    private final RecommendationRepository repository;
    private final ProfileServicePort profileServicePort;
    private final GenerativeAiPort generativeAiPort;
    private final StudentActivityLogRepository activityLogRepository;
    private final TaskServicePort taskServicePort;
    private final RecommendationMapper recommendationMapper;

    @Override
    public DailyRecommendationDTO execute(Long studentId, String requestType) {
        validateHistory(studentId);

        // Normalize requestType and handle FA-02
        String effectiveType = normalizeAndCheckData(studentId, requestType);

        Optional<Recommendation> existing = repository.findByStudentIdAndDateGenerated(studentId, LocalDate.now());
        if (existing.isPresent() && effectiveType.equals(existing.get().getRecommendationType())) {
            DailyRecommendationDTO dto = recommendationMapper.toDailyRecommendationDto(existing.get());
            dto.setMessage("Aquí tienes tus recomendaciones");
            return dto;
        }

        // 1. Armar el Contexto Orquestando diferentes fuentes
        String enrichedContext = buildEnrichedContext(studentId);

        // 2. Llamar a la API externa (protegida con Circuit Breaker + Retry)
        Recommendation newRecommendation = generativeAiPort.generateRecommendation(studentId, enrichedContext,
                effectiveType);

        // 3. Calcular confidenceScore interno basado en cantidad y calidad de datos
        double internalScore = calculateInternalConfidenceScore(studentId);
        double aiScore = newRecommendation.getConfidenceScore() != null ? newRecommendation.getConfidenceScore() : 0.0;
        double combinedScore = combineConfidenceScores(internalScore, aiScore);
        newRecommendation.setConfidenceScore(combinedScore);
        log.info("ConfidenceScore para studentId={}: interno={}, ia={}, combinado={}",
                studentId, internalScore, aiScore, combinedScore);

        // 4. Guardar y retornar
        Recommendation saved = repository.save(newRecommendation);
        DailyRecommendationDTO dto = recommendationMapper.toDailyRecommendationDto(saved);
        dto.setMessage("Aquí tienes tus recomendaciones");
        return dto;
    }

    private String normalizeAndCheckData(Long studentId, String requestType) {
        if (requestType == null || requestType.isBlank()) {
            return "GENERAL";
        }
        String type = requestType.toUpperCase();
        if (!type.equals("PRODUCTIVIDAD") && !type.equals("CARGA") && !type.equals("GENERAL")) {
            return "GENERAL";
        }

        // FA-02: Solicita tipo específico sin datos suficientes -> GENERAL
        if (type.equals("CARGA")) {
            try {
                List<TaskDTO> tasks = taskServicePort.getPrioritizedTasks(studentId);
                if (tasks == null || tasks.isEmpty()) {
                    log.info("FA-02: No hay tareas para recomendación de CARGA. Fallback a GENERAL.");
                    return "GENERAL";
                }
            } catch (Exception e) {
                return "GENERAL";
            }
        }
        return type;
    }

    private double calculateInternalConfidenceScore(Long studentId) {
        List<StudentActivityLog> logs = activityLogRepository.findByStudentId(studentId);

        if (logs.isEmpty()) {
            return 0.0;
        }

        double dataQuantityScore = Math.min(1.0, logs.size() / 50.0);
        double avgFocusScore = logs.stream()
                .filter(l -> l.getFocusScore() != null && l.getFocusScore() > 0)
                .mapToInt(StudentActivityLog::getFocusScore)
                .average()
                .orElse(50.0) / 100.0;

        LocalDateTime oldestDate = logs.stream()
                .map(StudentActivityLog::getLogDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        long daysOfHistory = ChronoUnit.DAYS.between(oldestDate, LocalDateTime.now());
        double historyScore = Math.min(1.0, daysOfHistory / 56.0);

        long uniqueSubjects = logs.stream()
                .map(StudentActivityLog::getSubject)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        double diversityScore = Math.min(1.0, uniqueSubjects / 5.0);

        double internalScore = (dataQuantityScore * 0.30)
                + (avgFocusScore * 0.25)
                + (historyScore * 0.25)
                + (diversityScore * 0.20);

        return Math.round(internalScore * 100.0) / 100.0;
    }

    private double combineConfidenceScores(double internalScore, double aiScore) {
        if (aiScore <= 0.0) {
            return internalScore;
        }
        double combined = (internalScore * 0.6) + (aiScore * 0.4);
        return Math.round(combined * 100.0) / 100.0;
    }

    private String buildEnrichedContext(Long studentId) {
        StringBuilder context = new StringBuilder();

        String learningStyle = profileServicePort.getLearningStyle(studentId);
        context.append("Estilo de aprendizaje del estudiante: ").append(learningStyle).append(".\n");

        List<StudentActivityLog> logs = activityLogRepository.findByStudentId(studentId);
        if (!logs.isEmpty()) {
            context.append("Historial reciente de actividad:\n");
            for (StudentActivityLog activityLog : logs) {
                context.append("- Materia: ").append(activityLog.getSubject())
                        .append(" | Acción: ").append(activityLog.getActivityType())
                        .append(" | Completado a las: ")
                        .append(activityLog.getActualCompletionTime() != null
                                ? activityLog.getActualCompletionTime().toLocalTime()
                                : "N/A")
                        .append("\n");
            }
        } else {
            context.append("No hay historial de actividad previo.\n");
        }

        try {
            List<TaskDTO> pendingTasks = taskServicePort.getPrioritizedTasks(studentId);
            if (pendingTasks != null && !pendingTasks.isEmpty()) {
                context.append("\nTareas priorizadas por el engine de planificación (ordenadas por urgencia):\n");
                for (TaskDTO task : pendingTasks) {
                    String title = task.getTitle() != null ? task.getTitle() : "Tarea";
                    Double priorityScore = task.getPriorityScore() != null ? task.getPriorityScore() : 0.0;
                    Integer estimatedMinutes = task.getEstimatedDurationMinutes() != null
                            ? task.getEstimatedDurationMinutes()
                            : 0;

                    context.append("- ").append(title)
                            .append(" [Materia: ").append(task.getSubjectId()).append("]")
                            .append(" | Prioridad: ").append(task.getPriorityLevel())
                            .append(" (score: ").append(String.format("%.1f", priorityScore)).append(")")
                            .append(" | Deadline: ")
                            .append(task.getDeadline() != null ? task.getDeadline().toLocalDate() : "sin fecha")
                            .append(" | Duración estimada: ").append(estimatedMinutes).append(" min")
                            .append("\n");
                }

                int totalMinutes = pendingTasks.stream()
                        .mapToInt(task -> task.getEstimatedDurationMinutes() != null
                                ? task.getEstimatedDurationMinutes()
                                : 0)
                        .sum();
                long criticalCount = pendingTasks.stream()
                        .filter(t -> "CRITICAL".equalsIgnoreCase(t.getPriorityLevel()))
                        .count();
                long urgentCount = pendingTasks.stream()
                        .filter(t -> t.getDeadline() != null
                                && !t.getDeadline().toLocalDate().isAfter(java.time.LocalDate.now().plusDays(1)))
                        .count();

                context.append("Resumen de carga: ")
                        .append(pendingTasks.size()).append(" tareas pendientes, ")
                        .append(totalMinutes).append(" minutos totales estimados, ")
                        .append(criticalCount).append(" en prioridad CRITICAL, ")
                        .append(urgentCount).append(" con deadline en las próximas 24h.\n");
            } else {
                context.append("No hay tareas pendientes registradas.\n");
            }
        } catch (Exception e) {
            log.warn("No se pudieron obtener tareas del planning-service para studentId={}: {}", studentId,
                    e.getMessage());
            context.append("No se pudieron obtener las tareas pendientes en este momento.\n");
        }

        return context.toString();
    }

    private void validateHistory(Long studentId) {
        Optional<StudentActivityLog> oldestLog = activityLogRepository.findFirstByStudentIdOrderByLogDateAsc(studentId);

        if (oldestLog.isEmpty() || oldestLog.get().getLogDate() == null
                || oldestLog.get().getLogDate().isAfter(LocalDateTime.now().minusDays(14))) {
            throw new InsufficientHistoryException(
                    "Aún no hay suficientes datos para generar recomendaciones. Continúa usando la aplicación.");
        }
    }

}
