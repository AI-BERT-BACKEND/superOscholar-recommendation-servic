package com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * DTO compatible con el formato de la API de Groq (OpenAI-compatible).
 * Usado como alternativa cuando Gemini no está disponible.
 */
@Data
@Builder
public class GroqRequest {
    private String model;
    private List<Message> messages;
    private Integer max_tokens;
    private Double temperature;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
