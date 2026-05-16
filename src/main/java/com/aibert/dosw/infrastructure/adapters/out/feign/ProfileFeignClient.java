package com.aibert.dosw.infrastructure.adapters.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client real hacia el profile-service para obtener datos del perfil del
 * estudiante.
 * URL configurable vía ${services.profile.url}.
 */
@FeignClient(name = "profileServiceClient", url = "${services.profile.url}")
public interface ProfileFeignClient {

    @GetMapping("/api/v1/profiles/{studentId}/learning-style")
    String getLearningStyle(@PathVariable("studentId") String studentId);
}
