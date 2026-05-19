package com.aibert.dosw.infrastructure.adapters.in.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que:
 * 1. Asigna un requestId único a cada petición (X-Request-Id header o UUID
 * auto-generado).
 * 2. Inyecta ese requestId en el MDC para que aparezca en todos los logs del
 * hilo.
 * 3. Propaga el requestId en el response header para trazabilidad end-to-end.
 * 4. Logea entrada y salida de cada petición con método, path, status y
 * duración.
 */
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        long startTime = System.currentTimeMillis();
        try {
            log.info(">>> {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            log.info("<<< {} {} → HTTP {} ({}ms)",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), durationMs);
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // No loguear peticiones a Swagger ni a actuator/health para reducir ruido
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/actuator/health");
    }
}
