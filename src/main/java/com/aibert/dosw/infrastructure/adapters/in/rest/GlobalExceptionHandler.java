package com.aibert.dosw.infrastructure.adapters.in.rest;

import com.aibert.dosw.domain.exception.InsufficientHistoryException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        // ── Dominio ───────────────────────────────────────────────────────────────

        @ExceptionHandler(InsufficientHistoryException.class)
        public ResponseEntity<Map<String, Object>> handleInsufficientHistory(
                        InsufficientHistoryException ex, HttpServletRequest request) {
                log.info("InsufficientHistory path={} message={}", request.getRequestURI(), ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(buildBody(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null,
                                                request.getRequestURI()));
        }

        // ── Validación de entrada ─────────────────────────────────────────────────

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidationErrors(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {
                String errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                log.warn("ValidationError path={} errors=[{}]", request.getRequestURI(), errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildBody(HttpStatus.BAD_REQUEST, errors, null, request.getRequestURI()));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<Map<String, Object>> handleUnreadableBody(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {
                log.warn("UnreadableBody path={} detail={}", request.getRequestURI(), ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildBody(HttpStatus.BAD_REQUEST, "JSON inválido o malformado.", null,
                                                request.getRequestURI()));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<Map<String, Object>> handleTypeMismatch(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
                String message = "Parámetro inválido '" + ex.getName() + "': se esperaba " + ex.getRequiredType();
                log.warn("TypeMismatch path={} param={}", request.getRequestURI(), ex.getName());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildBody(HttpStatus.BAD_REQUEST, message, null, request.getRequestURI()));
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<Map<String, Object>> handleMissingParam(
                        MissingServletRequestParameterException ex, HttpServletRequest request) {
                String message = "Falta el parámetro requerido: " + ex.getParameterName();
                log.warn("MissingParam path={} param={}", request.getRequestURI(), ex.getParameterName());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(buildBody(HttpStatus.BAD_REQUEST, message, null, request.getRequestURI()));
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
                log.warn("MethodNotAllowed path={} method={}", request.getRequestURI(), ex.getMethod());
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(buildBody(HttpStatus.METHOD_NOT_ALLOWED,
                                                "Método HTTP no permitido: " + ex.getMethod(),
                                                null, request.getRequestURI()));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<Map<String, Object>> handleResponseStatus(
                        ResponseStatusException ex, HttpServletRequest request) {
                HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
                String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
                log.warn("ResponseStatusException path={} status={} message={}", request.getRequestURI(), status,
                                message);
                return ResponseEntity.status(status)
                                .body(buildBody(status, message, null, request.getRequestURI()));
        }

        // ── Servicios externos (Feign / Circuit Breaker) ──────────────────────────

        @ExceptionHandler(FeignException.NotFound.class)
        public ResponseEntity<Map<String, Object>> handleFeignNotFound(
                        FeignException.NotFound ex, HttpServletRequest request) {
                log.warn("FeignNotFound path={} feignUrl={}", request.getRequestURI(), ex.request().url());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(buildBody(HttpStatus.NOT_FOUND,
                                                "Recurso no encontrado en servicio externo.", null,
                                                request.getRequestURI()));
        }

        @ExceptionHandler(FeignException.class)
        public ResponseEntity<Map<String, Object>> handleFeignException(
                        FeignException ex, HttpServletRequest request) {
                String errorId = UUID.randomUUID().toString();
                log.error("FeignException errorId={} httpStatus={} path={} feignUrl={}",
                                errorId, ex.status(), request.getRequestURI(), ex.request().url(), ex);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                                .body(buildBody(HttpStatus.BAD_GATEWAY,
                                                "Error al comunicarse con un servicio externo. Intenta más tarde.",
                                                errorId, request.getRequestURI()));
        }

        @ExceptionHandler(CallNotPermittedException.class)
        public ResponseEntity<Map<String, Object>> handleCircuitBreakerOpen(
                        CallNotPermittedException ex, HttpServletRequest request) {
                String errorId = UUID.randomUUID().toString();
                log.warn("CircuitBreakerOpen errorId={} path={} circuitBreaker={}",
                                errorId, request.getRequestURI(), ex.getCausingCircuitBreakerName());
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(buildBody(HttpStatus.SERVICE_UNAVAILABLE,
                                                "Servicio de IA temporalmente no disponible. El sistema está en modo protegido. Reintenta en 30s.",
                                                errorId, request.getRequestURI()));
        }

        // ── Persistencia ──────────────────────────────────────────────────────────

        @ExceptionHandler(DataAccessException.class)
        public ResponseEntity<Map<String, Object>> handleDataAccess(
                        DataAccessException ex, HttpServletRequest request) {
                String errorId = UUID.randomUUID().toString();
                log.error("DatabaseError errorId={} method={} path={}",
                                errorId, request.getMethod(), request.getRequestURI(), ex);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(buildBody(HttpStatus.SERVICE_UNAVAILABLE,
                                                "Error de acceso a datos. Intenta más tarde.", errorId,
                                                request.getRequestURI()));
        }

        // ── Fallback general ──────────────────────────────────────────────────────

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, HttpServletRequest request) {
                String errorId = UUID.randomUUID().toString();
                log.error("UnhandledException errorId={} method={} path={}",
                                errorId, request.getMethod(), request.getRequestURI(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(buildBody(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Ocurrió un error inesperado. Por favor, intente más tarde.", errorId,
                                                request.getRequestURI()));
        }

        // ── Helper ────────────────────────────────────────────────────────────────

        private Map<String, Object> buildBody(HttpStatus status, String message, String errorId, String path) {
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", status.value());
                body.put("error", status.getReasonPhrase());
                body.put("message", message);
                body.put("path", path);
                if (errorId != null) {
                        body.put("errorId", errorId);
                }
                return body;
        }
}
