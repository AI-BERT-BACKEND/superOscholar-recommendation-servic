package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiRequest;
import com.aibert.dosw.infrastructure.adapters.out.api.gemini.dto.GeminiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "externalAiClient", url = "${gemini.api.base-url}")
public interface ExternalAIClient {

    @PostMapping(value = "/models/gemini-1.5-flash:generateContent", consumes = "application/json")
    GeminiResponse generateContent(@RequestParam("key") String apiKey, @RequestBody GeminiRequest request);
}
