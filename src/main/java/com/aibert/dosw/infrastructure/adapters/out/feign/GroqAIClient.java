package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign Client para la API de Groq (formato OpenAI-compatible).
 * Gratuito, sin tarjeta de crédito, ultra rápido.
 * Docs: https://console.groq.com/docs/api-reference
 */
@FeignClient(name = "groqAiClient", url = "${groq.api.base-url}")
public interface GroqAIClient {

    @PostMapping(value = "/chat/completions", consumes = "application/json")
    GroqResponse chatCompletion(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody GroqRequest request);
}
