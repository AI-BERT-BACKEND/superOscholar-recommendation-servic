package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.groq.dto.GroqResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign Client para la API de Mistral AI (formato OpenAI-compatible).
 * Capa gratuita disponible en console.mistral.ai
 * Docs: https://docs.mistral.ai/api/
 */
@FeignClient(name = "mistralAiClient", url = "${mistral.api.base-url}")
public interface MistralAIClient {

    @PostMapping(value = "/chat/completions", consumes = "application/json")
    GroqResponse chatCompletion(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody GroqRequest request);
}
