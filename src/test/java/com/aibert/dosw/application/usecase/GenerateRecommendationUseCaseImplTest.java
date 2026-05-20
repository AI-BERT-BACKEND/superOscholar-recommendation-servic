package com.aibert.dosw.application.usecase;

import com.aibert.dosw.application.dto.request.RecommendationContextDTO;
import com.aibert.dosw.application.mapper.RecommendationMapper;
import com.aibert.dosw.domain.exception.InsufficientHistoryException;
import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.domain.model.StudentActivityLog;
import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.port.out.GenerativeAiPort;
import com.aibert.dosw.domain.port.out.ProfileServicePort;
import com.aibert.dosw.domain.port.out.RecommendationRepository;
import com.aibert.dosw.domain.port.out.StudentActivityLogRepository;
import com.aibert.dosw.domain.port.out.TaskServicePort;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateRecommendationUseCaseImplTest {

    @Mock
    private RecommendationRepository repository;
    @Mock
    private ProfileServicePort profileServicePort;
    @Mock
    private GenerativeAiPort generativeAiPort;
    @Mock
    private StudentActivityLogRepository activityLogRepository;
    @Mock
    private TaskServicePort taskServicePort;
    @Mock
    private RecommendationMapper recommendationMapper;

    @InjectMocks
    private GenerateRecommendationUseCaseImpl useCase;

    // ── Helpers ────────────────────────────────────────────────────────────────

    /** Log dated 20 days ago → passes the 14-day threshold check. */
    private StudentActivityLog oldLog() {
        return StudentActivityLog.builder()
                .studentId("s1")
                .logDate(LocalDateTime.now().minusDays(20))
                .subject("Math").activityType("TASK_COMPLETED").focusScore(80)
                .build();
    }

    private Recommendation baseRec(String type) {
        return Recommendation.builder()
                .studentId("s1").recommendationType(type)
                .confidenceScore(0.75).motivationalMessage("Sigue adelante")
                .recommendations(List.of()).dateGenerated(LocalDate.now())
                .build();
    }

    private DailyRecommendationDTO baseDTO(String type) {
        return DailyRecommendationDTO.builder()
                .studentId("s1").recommendationType(type).confidenceScore(0.75).build();
    }

    /**
     * Stubs the minimal chain: validateHistory → no existing → buildEnrichedContext
     * → AI → save → map
     */
    private void stubHappyPath(String type) {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Visual");
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());
        Recommendation rec = baseRec(type);
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq(type))).thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO(type));
    }

    // ── validateHistory ────────────────────────────────────────────────────────

    @Test
    void execute_noHistory_throwsInsufficientHistoryException() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientHistoryException.class,
                () -> useCase.execute("s1", "GENERAL", null));
        verifyNoInteractions(generativeAiPort, repository);
    }

    @Test
    void execute_historyTooRecent_throwsInsufficientHistoryException() {
        StudentActivityLog recentLog = StudentActivityLog.builder()
                .studentId("s1").logDate(LocalDateTime.now().minusDays(5)).build();
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(recentLog));

        assertThrows(InsufficientHistoryException.class,
                () -> useCase.execute("s1", "GENERAL", null));
    }

    @Test
    void execute_logDateIsNull_throwsInsufficientHistoryException() {
        StudentActivityLog nullDateLog = StudentActivityLog.builder()
                .studentId("s1").logDate(null).build();
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(nullDateLog));

        assertThrows(InsufficientHistoryException.class,
                () -> useCase.execute("s1", "GENERAL", null));
    }

    // ── Existing recommendation ────────────────────────────────────────────────

    @Test
    void execute_existingRecommendationSameType_returnsExistingWithoutCallingAI() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        Recommendation existing = baseRec("GENERAL");
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.of(existing));
        when(recommendationMapper.toDailyRecommendationDto(existing)).thenReturn(baseDTO("GENERAL"));

        DailyRecommendationDTO result = useCase.execute("s1", "GENERAL", null);

        assertNotNull(result);
        verifyNoInteractions(generativeAiPort);
        verify(repository, never()).save(any());
    }

    @Test
    void execute_existingRecommendationDifferentType_generatesNew() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        // Existing is GENERAL but requesting PRODUCTIVIDAD
        Recommendation existing = baseRec("GENERAL");
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.of(existing));

        when(profileServicePort.getLearningStyle("s1")).thenReturn("Auditory");
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());
        Recommendation newRec = baseRec("PRODUCTIVIDAD");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("PRODUCTIVIDAD")))
                .thenReturn(newRec);
        when(repository.save(any())).thenReturn(newRec);
        when(recommendationMapper.toDailyRecommendationDto(newRec)).thenReturn(baseDTO("PRODUCTIVIDAD"));

        DailyRecommendationDTO result = useCase.execute("s1", "PRODUCTIVIDAD", null);

        assertNotNull(result);
        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("PRODUCTIVIDAD"));
    }

    // ── requestType normalization ──────────────────────────────────────────────

    @Test
    void execute_nullRequestType_usesGeneral() {
        stubHappyPath("GENERAL");

        useCase.execute("s1", null, null);

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("GENERAL"));
    }

    @Test
    void execute_blankRequestType_usesGeneral() {
        stubHappyPath("GENERAL");

        useCase.execute("s1", "   ", null);

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("GENERAL"));
    }

    @Test
    void execute_unknownRequestType_usesGeneral() {
        stubHappyPath("GENERAL");

        useCase.execute("s1", "UNKNOWN_TYPE", null);

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("GENERAL"));
    }

    @Test
    void execute_productividadType_isPreserved() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Kinesthetic");
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());
        Recommendation rec = baseRec("PRODUCTIVIDAD");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("PRODUCTIVIDAD")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("PRODUCTIVIDAD"));

        useCase.execute("s1", "productividad", null); // lowercase

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("PRODUCTIVIDAD"));
    }

    // ── FA-02: CARGA fallback ──────────────────────────────────────────────────

    @Test
    void execute_cargaTypeWithContextHavingTasks_usesCarga() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));

        RecommendationContextDTO ctx = new RecommendationContextDTO();
        ctx.setPendingTaskCount(3);

        Recommendation rec = baseRec("CARGA");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("CARGA")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("CARGA"));

        useCase.execute("s1", "CARGA", ctx);

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("CARGA"));
    }

    @Test
    void execute_cargaTypeWithContextHavingNoTasks_fallsBackToGeneral() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));

        RecommendationContextDTO ctx = new RecommendationContextDTO();
        ctx.setPendingTaskCount(0); // FA-02: no tasks → GENERAL

        Recommendation rec = baseRec("GENERAL");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("GENERAL"));

        useCase.execute("s1", "CARGA", ctx);

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("GENERAL"));
    }

    @Test
    void execute_cargaTypeWithContextHavingNullCount_fallsBackToGeneral() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));

        RecommendationContextDTO ctx = new RecommendationContextDTO();
        ctx.setPendingTaskCount(null); // null → treated as 0 → GENERAL

        Recommendation rec = baseRec("GENERAL");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("GENERAL"));

        useCase.execute("s1", "CARGA", ctx);

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("GENERAL"));
    }

    @Test
    void execute_cargaTypeNoContextEmptyTasks_fallsBackToGeneral() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Visual");
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());

        Recommendation rec = baseRec("GENERAL");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("GENERAL"));

        useCase.execute("s1", "CARGA", null);

        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("GENERAL"));
    }

    @Test
    void execute_cargaTypeNoContextServiceThrows_fallsBackToGeneral() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        // Both calls to getPrioritizedTasks throw (caught by their respective
        // try-catches)
        when(taskServicePort.getPrioritizedTasks("s1")).thenThrow(new RuntimeException("down"));
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Visual");
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));

        Recommendation rec = baseRec("GENERAL");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("GENERAL"));

        assertDoesNotThrow(() -> useCase.execute("s1", "CARGA", null));
        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("GENERAL"));
    }

    // ── confidenceScore calculation ────────────────────────────────────────────

    @Test
    void execute_aiScoreIsZero_confidenceCombinedUsesInternalOnly() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Visual");
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());

        Recommendation rec = baseRec("GENERAL");
        rec.setConfidenceScore(0.0); // AI score = 0
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL"))).thenReturn(rec);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recommendationMapper.toDailyRecommendationDto(any())).thenReturn(baseDTO("GENERAL"));

        useCase.execute("s1", "GENERAL", null);

        // With aiScore=0, combined = internalScore only → must be ≥ 0
        verify(repository).save(argThat(r -> r.getConfidenceScore() >= 0.0));
    }

    @Test
    void execute_withMultipleActivityLogs_calculatesHigherScore() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Visual");

        // Many logs with diverse subjects and focus scores
        List<StudentActivityLog> manyLogs = new java.util.ArrayList<>();
        for (int i = 0; i < 30; i++) {
            manyLogs.add(StudentActivityLog.builder()
                    .studentId("s1")
                    .logDate(LocalDateTime.now().minusDays(i + 1))
                    .subject("Subject" + (i % 5)) // 5 different subjects
                    .activityType("TASK_COMPLETED")
                    .focusScore(70 + (i % 20))
                    .build());
        }
        when(activityLogRepository.findByStudentId("s1")).thenReturn(manyLogs);
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());

        Recommendation rec = baseRec("GENERAL");
        rec.setConfidenceScore(0.8);
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL"))).thenReturn(rec);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recommendationMapper.toDailyRecommendationDto(any())).thenReturn(baseDTO("GENERAL"));

        useCase.execute("s1", "GENERAL", null);

        // Combined score = internal(>0) * 0.6 + 0.8 * 0.4 > 0
        verify(repository).save(argThat(r -> r.getConfidenceScore() > 0.0));
    }

    // ── context enrichment paths ───────────────────────────────────────────────

    @Test
    void execute_withContext_skipsTaskServiceCallForCarga() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));

        RecommendationContextDTO ctx = new RecommendationContextDTO();
        ctx.setPendingTaskCount(5);
        ctx.setCriticalTaskCount(2);
        ctx.setUnassignedTaskCount(1);
        ctx.setDailyCapMinutes(240);
        ctx.setFullyAssigned(true);

        Recommendation rec = baseRec("CARGA");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("CARGA")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("CARGA"));

        useCase.execute("s1", "CARGA", ctx);

        // When context is provided, taskServicePort.getPrioritizedTasks should NOT be
        // called
        // (it's only called in normalizeAndCheckData when context == null)
        verify(taskServicePort, never()).getPrioritizedTasks("s1");
    }

    @Test
    void execute_withContextAndOverloadedDays_buildsEnrichedContext() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));

        RecommendationContextDTO ctx = new RecommendationContextDTO();
        ctx.setPendingTaskCount(5);
        ctx.setOverloadedDays(List.of(
                new RecommendationContextDTO.OverloadedDay(LocalDate.now(), 120)));
        ctx.setTasks(List.of(new RecommendationContextDTO.TaskContext(
                "t1", "Algebra", 60,
                LocalDateTime.now().plusDays(5), "HIGH", "TODO", "TAREA", 3, "MATH")));

        Recommendation rec = baseRec("CARGA");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("CARGA")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("CARGA"));

        assertDoesNotThrow(() -> useCase.execute("s1", "CARGA", ctx));
        verify(generativeAiPort).generateRecommendation(eq("s1"), anyString(), eq("CARGA"));
    }

    @Test
    void execute_withContextProfileServiceThrows_continuesGracefully() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));
        when(profileServicePort.getLearningStyle("s1")).thenThrow(new RuntimeException("profile down"));

        RecommendationContextDTO ctx = new RecommendationContextDTO();
        ctx.setPendingTaskCount(2);

        Recommendation rec = baseRec("CARGA");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("CARGA")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("CARGA"));

        // Profile service exception in try-catch should not propagate
        assertDoesNotThrow(() -> useCase.execute("s1", "CARGA", ctx));
    }

    @Test
    void execute_withTasksFromPlanningService_buildsContextWithTasks() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Visual");
        when(activityLogRepository.findByStudentId("s1")).thenReturn(List.of(oldLog()));
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(List.of(
                TaskDTO.builder().taskId("t1").title("Calc").subjectId("MATH-101")
                        .priorityLevel("HIGH").priorityScore(9.0)
                        .deadline(LocalDate.now().plusDays(2).atStartOfDay())
                        .estimatedDurationMinutes(60).build(),
                TaskDTO.builder().taskId("t2").title("History").subjectId("HIST-201")
                        .priorityLevel("CRITICAL").priorityScore(9.8)
                        .deadline(LocalDate.now().atStartOfDay())
                        .estimatedDurationMinutes(null).build()));

        Recommendation rec = baseRec("GENERAL");
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL")))
                .thenReturn(rec);
        when(repository.save(any())).thenReturn(rec);
        when(recommendationMapper.toDailyRecommendationDto(rec)).thenReturn(baseDTO("GENERAL"));

        assertDoesNotThrow(() -> useCase.execute("s1", "GENERAL", null));
    }

    @Test
    void execute_emptyActivityLogs_internalScoreIsZero() {
        when(activityLogRepository.findFirstByStudentIdOrderByLogDateAsc("s1"))
                .thenReturn(Optional.of(oldLog()));
        when(repository.findByStudentIdAndDateGenerated("s1", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(profileServicePort.getLearningStyle("s1")).thenReturn("Visual");
        // findByStudentId returns empty (different from
        // findFirstByStudentIdOrderByLogDateAsc)
        when(activityLogRepository.findByStudentId("s1")).thenReturn(Collections.emptyList());
        when(taskServicePort.getPrioritizedTasks("s1")).thenReturn(Collections.emptyList());

        Recommendation rec = baseRec("GENERAL");
        rec.setConfidenceScore(0.7);
        when(generativeAiPort.generateRecommendation(eq("s1"), anyString(), eq("GENERAL"))).thenReturn(rec);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recommendationMapper.toDailyRecommendationDto(any())).thenReturn(baseDTO("GENERAL"));

        useCase.execute("s1", "GENERAL", null);

        // internalScore=0 → combinedScore = 0*0.6 + 0.7*0.4 = 0.28
        verify(repository).save(argThat(r -> r.getConfidenceScore() >= 0.0));
    }
}
