package com.aibert.dosw.infrastructure.adapters.out.api.groq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * DTO compatible con el formato de la API de Groq (OpenAI-compatible).
 * Usado para solicitudes al proveedor Groq.
 */
@Data
@Builder
public class GroqRequest {
    private String model;
    private List<Message> messages;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private Double temperature;

    @Data
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
