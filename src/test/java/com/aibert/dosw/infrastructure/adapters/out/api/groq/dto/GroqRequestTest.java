package com.aibert.dosw.infrastructure.adapters.out.api.groq.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void lombokGeneratedMethods_areCoveredForRequestAndMessage() {
        GroqRequest.Message message1 = GroqRequest.Message.builder().role("user").content("hola").build();
        GroqRequest.Message message2 = GroqRequest.Message.builder().role("user").content("hola").build();
        GroqRequest.Message message3 = GroqRequest.Message.builder().role("system").content("ctx").build();

        assertEquals(message1, message2);
        assertNotEquals(message1, message3);
        assertNotEquals(message1, null);
        assertNotEquals(message1, new Object());
        assertEquals(message1.hashCode(), message2.hashCode());
        assertNotNull(message1.toString());

        GroqRequest request1 = GroqRequest.builder()
                .model("m1")
                .messages(List.of(message1))
                .maxTokens(120)
                .temperature(0.4)
                .build();
        GroqRequest request2 = GroqRequest.builder()
                .model("m1")
                .messages(List.of(message2))
                .maxTokens(120)
                .temperature(0.4)
                .build();
        GroqRequest request3 = GroqRequest.builder()
                .model("m2")
                .messages(List.of(message3))
                .maxTokens(200)
                .temperature(0.9)
                .build();

        assertEquals(request1, request2);
        assertNotEquals(request1, request3);
        assertNotEquals(request1, null);
        assertNotEquals(request1, new Object());
        assertEquals(request1.hashCode(), request2.hashCode());
        assertEquals("m1", request1.getModel());
        assertEquals(120, request1.getMaxTokens());
        assertEquals(0.4, request1.getTemperature());
        assertNotNull(request1.toString());
    }
}
