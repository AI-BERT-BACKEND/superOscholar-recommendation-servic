package com.aibert.dosw.infrastructure.adapters.in.rest;

import com.aibert.dosw.application.dto.request.RecommendationContextDTO;
import com.aibert.dosw.application.dto.request.RecommendationRequest;
import com.aibert.dosw.domain.exception.InsufficientHistoryException;
import com.aibert.dosw.domain.port.in.GenerateDailyPlanUseCase;
import com.aibert.dosw.domain.port.in.GenerateRecommendationUseCase;
import com.aibert.dosw.domain.port.in.GenerateWeeklyReorganizationUseCase;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyPlanDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.WeeklyReorganizationDTO;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    @Mock
    private GenerateRecommendationUseCase generateRecommendationUseCase;
    @Mock
    private GenerateDailyPlanUseCase generateDailyPlanUseCase;
    @Mock
    private GenerateWeeklyReorganizationUseCase generateWeeklyReorganizationUseCase;

    @InjectMocks
    private RecommendationController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ── POST /api/v1/recommendations ──────────────────────────────────────────

    @Test
    void generateRecommendations_validRequest_returns200() throws Exception {
        RecommendationRequest req = new RecommendationRequest("student123", "GENERAL", null);
        DailyRecommendationDTO resp = DailyRecommendationDTO.builder()
                .studentId("student123").recommendationType("GENERAL").confidenceScore(0.75).build();

        when(generateRecommendationUseCase.execute("student123", "GENERAL", null)).thenReturn(resp);

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId", is("student123")))
                .andExpect(jsonPath("$.recommendationType", is("GENERAL")));
    }

    @Test
    void generateRecommendations_missingStudentId_returns400() throws Exception {
        RecommendationRequest req = new RecommendationRequest("", "GENERAL", null);

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void generateRecommendations_nullStudentId_returns400() throws Exception {
        // studentId is @NotBlank → null triggers violation
        String body = "{\"studentId\":null,\"requestType\":\"GENERAL\"}";

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateRecommendations_invalidRequestType_returns400() throws Exception {
        String body = "{\"studentId\":\"s1\",\"requestType\":\"INVALID\"}";

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    void generateRecommendations_insufficientHistory_returns422() throws Exception {
        RecommendationRequest req = new RecommendationRequest("s1", "GENERAL", null);

        when(generateRecommendationUseCase.execute("s1", "GENERAL", null))
                .thenThrow(new InsufficientHistoryException("Historial insuficiente"));

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status", is(422)));
    }

    @Test
    void generateRecommendations_malformedJson_returns400() throws Exception {
        String invalidJson = "{ not valid json }";

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateRecommendations_wrongHttpMethod_returns405() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void generateRecommendations_withContext_passesContextToUseCase() throws Exception {
        RecommendationContextDTO ctx = new RecommendationContextDTO();
        ctx.setPendingTaskCount(3);
        ctx.setDailyCapMinutes(240);
        RecommendationRequest req = new RecommendationRequest("s1", "CARGA", ctx);

        DailyRecommendationDTO resp = DailyRecommendationDTO.builder()
                .studentId("s1").recommendationType("CARGA").confidenceScore(0.5).build();

        when(generateRecommendationUseCase.execute(eq("s1"), eq("CARGA"), any(RecommendationContextDTO.class)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendationType", is("CARGA")));
    }

    @Test
    void generateRecommendations_internalError_returns500() throws Exception {
        RecommendationRequest req = new RecommendationRequest("s1", "GENERAL", null);

        when(generateRecommendationUseCase.execute(any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status", is(500)));
    }

    // ── GET /api/v1/recommendations/daily/{studentId} ─────────────────────────

    @Test
    void getDailyPlan_withStudentId_returns200() throws Exception {
        DailyPlanDTO plan = DailyPlanDTO.builder()
                .studentId("s1").planDate(LocalDate.now())
                .todayTasks(Collections.emptyList())
                .reschedulableTasks(Collections.emptyList())
                .reorganization(Collections.emptyList())
                .totalEstimatedMinutes(0).urgentAlert(false)
                .message("No tienes tareas pendientes para hoy. ¡Buen trabajo!")
                .build();

        when(generateDailyPlanUseCase.execute(eq("s1"), any(LocalDate.class))).thenReturn(plan);

        mockMvc.perform(get("/api/v1/recommendations/daily/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId", is("s1")));
    }

    @Test
    void getDailyPlan_withExplicitDate_passesDateToUseCase() throws Exception {
        LocalDate specificDate = LocalDate.of(2026, 5, 19);
        DailyPlanDTO plan = DailyPlanDTO.builder()
                .studentId("s1").planDate(specificDate)
                .todayTasks(Collections.emptyList())
                .reschedulableTasks(Collections.emptyList())
                .reorganization(Collections.emptyList())
                .totalEstimatedMinutes(0).urgentAlert(false)
                .message("Plan listo").build();

        when(generateDailyPlanUseCase.execute("s1", specificDate)).thenReturn(plan);

        mockMvc.perform(get("/api/v1/recommendations/daily/s1")
                .param("currentDate", "2026-05-19"))
                .andExpect(status().isOk());

        verify(generateDailyPlanUseCase).execute("s1", specificDate);
    }

    @Test
    void getDailyPlan_withInvalidDate_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/daily/s1")
                .param("currentDate", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDailyPlan_withUrgentAlert_returnsUrgentAlertTrue() throws Exception {
        DailyPlanDTO plan = DailyPlanDTO.builder()
                .studentId("s1").planDate(LocalDate.now())
                .todayTasks(Collections.emptyList())
                .reschedulableTasks(Collections.emptyList())
                .reorganization(Collections.emptyList())
                .totalEstimatedMinutes(60).urgentAlert(true)
                .message("Alerta").build();

        when(generateDailyPlanUseCase.execute(eq("s1"), any(LocalDate.class))).thenReturn(plan);

        mockMvc.perform(get("/api/v1/recommendations/daily/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urgentAlert", is(true)));
    }

    // ── GET /api/v1/recommendations/weekly/{studentId} ────────────────────────

    @Test
    void getWeeklyPlan_returns200() throws Exception {
        WeeklyReorganizationDTO plan = WeeklyReorganizationDTO.builder()
                .studentId("s1").weekStartDate(LocalDate.now())
                .reorganizationSuggestions(Collections.emptyList())
                .reschedulableTasks(Collections.emptyList())
                .overloadedDays(Collections.emptyList())
                .message("Plan semanal").build();

        when(generateWeeklyReorganizationUseCase.execute(eq("s1"), any(LocalDate.class))).thenReturn(plan);

        mockMvc.perform(get("/api/v1/recommendations/weekly/s1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId", is("s1")));
    }

    @Test
    void getWeeklyPlan_withExplicitDate_passesDateToUseCase() throws Exception {
        LocalDate specificDate = LocalDate.of(2026, 5, 19);
        WeeklyReorganizationDTO plan = WeeklyReorganizationDTO.builder()
                .studentId("s1").weekStartDate(specificDate)
                .reorganizationSuggestions(Collections.emptyList())
                .reschedulableTasks(Collections.emptyList())
                .overloadedDays(Collections.emptyList())
                .message("Plan semanal").build();

        when(generateWeeklyReorganizationUseCase.execute("s1", specificDate)).thenReturn(plan);

        mockMvc.perform(get("/api/v1/recommendations/weekly/s1")
                .param("currentDate", "2026-05-19"))
                .andExpect(status().isOk());

        verify(generateWeeklyReorganizationUseCase).execute("s1", specificDate);
    }

    @Test
    void getWeeklyPlan_withInvalidDate_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/weekly/s1")
                .param("currentDate", "bad-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getWeeklyPlan_serviceThrowsDataAccessException_returns503() throws Exception {
        when(generateWeeklyReorganizationUseCase.execute(any(), any()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB down"));

        mockMvc.perform(get("/api/v1/recommendations/weekly/s1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is(503)));
    }

    // ── GlobalExceptionHandler — Feign / Circuit Breaker / ResponseStatus ────

    @Test
    void generateRecommendations_feignNotFoundException_returns404() throws Exception {
        Request feignReq = Request.create(Request.HttpMethod.GET, "http://service/resource",
                java.util.Collections.emptyMap(), null, new RequestTemplate());
        when(generateRecommendationUseCase.execute(any(), any(), any()))
                .thenThrow(new FeignException.NotFound("not found", feignReq, null, null));

        String body = objectMapper.writeValueAsString(validRequest());
        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void generateRecommendations_feignException_returns502() throws Exception {
        Request feignReq = Request.create(Request.HttpMethod.POST, "http://service/ai",
                java.util.Collections.emptyMap(), null, new RequestTemplate());
        when(generateRecommendationUseCase.execute(any(), any(), any()))
                .thenThrow(new FeignException.ServiceUnavailable("svc unavailable", feignReq, null, null));

        String body = objectMapper.writeValueAsString(validRequest());
        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status", is(502)));
    }

    @Test
    void generateRecommendations_circuitBreakerOpen_returns503() throws Exception {
        CircuitBreaker cb = CircuitBreaker.ofDefaults("testCB");
        // Force circuit breaker to open state
        cb.transitionToOpenState();
        when(generateRecommendationUseCase.execute(any(), any(), any()))
                .thenThrow(CallNotPermittedException.createCallNotPermittedException(cb));

        String body = objectMapper.writeValueAsString(validRequest());
        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is(503)));
    }

    @Test
    void generateRecommendations_responseStatusException_returnsCorrectStatus() throws Exception {
        when(generateRecommendationUseCase.execute(any(), any(), any()))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "Estado conflictivo"));

        String body = objectMapper.writeValueAsString(validRequest());
        mockMvc.perform(post("/api/v1/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private RecommendationRequest validRequest() {
        RecommendationRequest req = new RecommendationRequest();
        req.setStudentId("s1");
        req.setRequestType("GENERAL");
        return req;
    }
}
