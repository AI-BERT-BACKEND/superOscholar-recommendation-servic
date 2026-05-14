package com.aibert.dosw.infrastructure.adapters.out.api.groq.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO para parsear la respuesta de Groq (formato OpenAI-compatible).
 */
@Data
public class GroqResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
