package com.aibert.dosw.application.usecase;

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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateRecommendationUseCaseImpl implements GenerateRecommendationUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateRecommendationUseCaseImpl.class);

    private final RecommendationRepository repository;
    private final ProfileServicePort profileServicePort;
    private final GenerativeAiPort generativeAiPort;
    private final StudentActivityLogRepository activityLogRepository;
    private final TaskServicePort taskServicePort;

    @Override
    public DailyRecommendationDTO execute(Long studentId) {
        validateHistory(studentId);

        Optional<Recommendation> existing = repository.findByStudentIdAndDateGenerated(studentId, LocalDate.now());
        if (existing.isPresent()) {
            return mapToDto(existing.get());
        }

        // 1. Armar el Contexto Orquestando diferentes fuentes
        String enrichedContext = buildEnrichedContext(studentId);

        // 2. Llamar a la API externa (protegida con Circuit Breaker + Retry)
        Recommendation newRecommendation = generativeAiPort.generateRecommendation(studentId, enrichedContext);

        // 3. Calcular confidenceScore interno basado en cantidad y calidad de datos (R18-T5)
        double internalScore = calculateInternalConfidenceScore(studentId);
        double aiScore = newRecommendation.getConfidenceScore() != null ? newRecommendation.getConfidenceScore() : 0.0;
        double combinedScore = combineConfidenceScores(internalScore, aiScore);
        newRecommendation.setConfidenceScore(combinedScore);
        log.info("ConfidenceScore para studentId={}: interno={}, ia={}, combinado={}",
                studentId, internalScore, aiScore, combinedScore);
        
        // 4. Guardar y retornar
        Recommendation saved = repository.save(newRecommendation);
        return mapToDto(saved);
    }

    /**
     * R18-T5: Calcula el confidenceScore basado en la cantidad y calidad de datos del estudiante.
     * 
     * Factores considerados:
     * - Cantidad de registros de actividad (30% del peso)
     * - Calidad de los datos: promedio de focusScore (25% del peso)
     * - Antigüedad del historial en semanas (25% del peso)
     * - Diversidad de materias estudiadas (20% del peso)
     * 
     * @return score entre 0.0 y 1.0
     */
    private double calculateInternalConfidenceScore(Long studentId) {
        List<StudentActivityLog> logs = activityLogRepository.findByStudentId(studentId);

        if (logs.isEmpty()) {
            return 0.0;
        }

        // Factor 1: Cantidad de datos (más logs = más confianza, se satura en 50 registros)
        double dataQuantityScore = Math.min(1.0, logs.size() / 50.0);

        // Factor 2: Calidad de datos (promedio de focusScore normalizado a 0-1)
        double avgFocusScore = logs.stream()
                .filter(l -> l.getFocusScore() != null && l.getFocusScore() > 0)
                .mapToInt(StudentActivityLog::getFocusScore)
                .average()
                .orElse(50.0) / 100.0;

        // Factor 3: Antigüedad del historial (más semanas = más confianza, se satura en 8 semanas)
        LocalDateTime oldestDate = logs.stream()
                .map(StudentActivityLog::getLogDate)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        long daysOfHistory = ChronoUnit.DAYS.between(oldestDate, LocalDateTime.now());
        double historyScore = Math.min(1.0, daysOfHistory / 56.0); // 56 días = 8 semanas

        // Factor 4: Diversidad de materias (más materias = mejor panorama, se satura en 5)
        long uniqueSubjects = logs.stream()
                .map(StudentActivityLog::getSubject)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        double diversityScore = Math.min(1.0, uniqueSubjects / 5.0);

        // Ponderación final
        double internalScore = (dataQuantityScore * 0.30)
                + (avgFocusScore * 0.25)
                + (historyScore * 0.25)
                + (diversityScore * 0.20);

        return Math.round(internalScore * 100.0) / 100.0;
    }

    /**
     * Combina el score interno (basado en datos) con el score de la IA.
     * - Si la IA no participó (aiScore == 0.0): usa solo el score interno.
     * - Si la IA participó: 60% interno + 40% IA para anclar la confianza a datos reales.
     */
    private double combineConfidenceScores(double internalScore, double aiScore) {
        if (aiScore <= 0.0) {
            // Fallback: solo score interno (la IA no generó nada útil)
            return internalScore;
        }
        double combined = (internalScore * 0.6) + (aiScore * 0.4);
        return Math.round(combined * 100.0) / 100.0;
    }

    private String buildEnrichedContext(Long studentId) {
        StringBuilder context = new StringBuilder();

        // Estilo de aprendizaje
        String learningStyle = profileServicePort.getLearningStyle(studentId);
        context.append("Estilo de aprendizaje del estudiante: ").append(learningStyle).append(".\n");

        // Historial de actividad (Patrones)
        List<StudentActivityLog> logs = activityLogRepository.findByStudentId(studentId);
        if (!logs.isEmpty()) {
            context.append("Historial reciente de actividad:\n");
            for (StudentActivityLog activityLog : logs) {
                context.append("- Materia: ").append(activityLog.getSubject())
                       .append(" | Acción: ").append(activityLog.getActivityType())
                       .append(" | Completado a las: ").append(activityLog.getActualCompletionTime() != null ? activityLog.getActualCompletionTime().toLocalTime() : "N/A")
                       .append("\n");
            }
        } else {
            context.append("No hay historial de actividad previo.\n");
        }

        // Tareas pendientes reales del planning-service (via Feign / Adapter)
        try {
            List<TaskDTO> pendingTasks = taskServicePort.getPrioritizedTasks(studentId);
            if (pendingTasks != null && !pendingTasks.isEmpty()) {
                context.append("\nTareas priorizadas por el engine de planificación (ordenadas por urgencia):\n");
                for (TaskDTO task : pendingTasks) {
                    context.append("- ").append(task.getTitle())
                           .append(" [Materia: ").append(task.getSubjectId()).append("]")
                           .append(" | Prioridad: ").append(task.getPriorityLevel())
                           .append(" (score: ").append(String.format("%.1f", task.getPriorityScore())).append(")")
                           .append(" | Deadline: ").append(task.getDeadline() != null ? task.getDeadline().toLocalDate() : "sin fecha")
                           .append(" | Duración estimada: ").append(task.getEstimatedDurationMinutes()).append(" min")
                           .append("\n");
                }

                // Métricas agregadas para la IA
                int totalMinutes = pendingTasks.stream()
                        .mapToInt(TaskDTO::getEstimatedDurationMinutes)
                        .sum();
                long criticalCount = pendingTasks.stream()
                        .filter(t -> "CRITICAL".equalsIgnoreCase(t.getPriorityLevel()))
                        .count();
                long urgentCount = pendingTasks.stream()
                        .filter(t -> t.getDeadline() != null && !t.getDeadline().toLocalDate().isAfter(java.time.LocalDate.now().plusDays(1)))
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
            log.warn("No se pudieron obtener tareas del planning-service para studentId={}: {}", studentId, e.getMessage());
            context.append("No se pudieron obtener las tareas pendientes en este momento.\n");
        }

        return context.toString();
    }

    private void validateHistory(Long studentId) {
        Optional<StudentActivityLog> oldestLog = activityLogRepository.findFirstByStudentIdOrderByLogDateAsc(studentId);
        
        if (oldestLog.isEmpty() || oldestLog.get().getLogDate() == null || oldestLog.get().getLogDate().isAfter(LocalDateTime.now().minusDays(14))) {
            throw new InsufficientHistoryException("El estudiante " + studentId + " no tiene suficientes datos. Se requieren al menos 2 semanas de actividad registrada para generar recomendaciones precisas.");
        }
    }

    private DailyRecommendationDTO mapToDto(Recommendation rec) {
        return DailyRecommendationDTO.builder()
                .studentId(rec.getStudentId())
                .motivationalMessage(rec.getMotivationalMessage())
                .studyTips(rec.getStudyTips())
                .dateGenerated(rec.getDateGenerated())
                .confidenceScore(rec.getConfidenceScore())
                .build();
    }
}
