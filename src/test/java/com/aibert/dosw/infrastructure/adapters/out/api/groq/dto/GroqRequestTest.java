package com.aibert.dosw.infrastructure.adapters.out.api.groq.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroqRequestTest {

    @Test
    void serializesMaxTokensAsSnakeCaseForGroqApi() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        GroqRequest request = GroqRequest.builder()
                .model("llama-3.3-70b-versatile")
                .messages(List.of(GroqRequest.Message.builder().role("user").content("hola").build()))
                .maxTokens(800)
                .temperature(0.7)
                .build();

        String json = objectMapper.writeValueAsString(request);

        assertTrue(json.contains("\"max_tokens\":800"));
        assertFalse(json.contains("\"maxTokens\""));
    }
}
