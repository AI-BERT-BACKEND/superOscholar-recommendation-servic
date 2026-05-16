package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign Client para la API de Gemini (Generative Language API).
 * Docs: https://ai.google.dev/api/generate-content
 */
@FeignClient(name = "geminiAiClient", url = "${gemini.api.base-url}")
public interface GeminiAIClient {

    @PostMapping(value = "/{model}:generateContent", consumes = "application/json")
    GeminiResponse generateContent(
            @PathVariable("model") String model,
            @RequestParam("key") String apiKey,
            @RequestBody GeminiRequest request);
}
