package com.aibert.dosw.application.usecase;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.model.StudentActivityLog;
import com.aibert.dosw.domain.port.in.GenerateRecommendationUseCase;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.domain.port.out.ProfileServicePort;
import com.aibert.dosw.domain.port.out.RecommendationRepository;
import com.aibert.dosw.domain.port.out.StudentActivityLogRepository;
import com.aibert.dosw.domain.exception.InsufficientHistoryException;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateRecommendationUseCaseImpl implements GenerateRecommendationUseCase {

    private final RecommendationRepository repository;
    private final ProfileServicePort profileServicePort;
    private final GenerativeAiPort generativeAiPort;
    private final StudentActivityLogRepository activityLogRepository;

    @Override
    public DailyRecommendationDTO execute(Long studentId) {
        validateHistory(studentId);

        Optional<Recommendation> existing = repository.findByStudentIdAndDateGenerated(studentId, LocalDate.now());
        if (existing.isPresent()) {
            return mapToDto(existing.get());
        }

        // 1. Armar el Contexto Orquestando diferentes fuentes
        String enrichedContext = buildEnrichedContext(studentId);

        // 2. Llamar a la API externa
        Recommendation newRecommendation = generativeAiPort.generateRecommendation(studentId, enrichedContext);
        
        // 3. Guardar y retornar
        Recommendation saved = repository.save(newRecommendation);
        return mapToDto(saved);
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
            for (StudentActivityLog log : logs) {
                context.append("- Materia: ").append(log.getSubject())
                       .append(" | Acción: ").append(log.getActivityType())
                       .append(" | Completado a las: ").append(log.getActualCompletionTime() != null ? log.getActualCompletionTime().toLocalTime() : "N/A")
                       .append("\n");
            }
        } else {
            context.append("No hay historial de actividad previo.\n");
        }

        // Tareas actuales (Mock por ahora)
        List<String> mockTasks = List.of("Matemáticas Avanzadas", "Historia del Arte");
        context.append("Tareas pendientes actuales: ").append(String.join(", ", mockTasks)).append(".\n");

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
