package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.port.out.ProfileServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptador que intenta obtener el estilo de aprendizaje del profile-service
 * real vía Feign.
 * Si el servicio no está disponible, retorna un valor por defecto como
 * fallback.
 */
@Component
public class ProfileFeignClientAdapter implements ProfileServicePort {

    private static final Logger log = LoggerFactory.getLogger(ProfileFeignClientAdapter.class);
    private static final String DEFAULT_LEARNING_STYLE = "VISUAL";

    private final ProfileFeignClient profileFeignClient;

    public ProfileFeignClientAdapter(ProfileFeignClient profileFeignClient) {
        this.profileFeignClient = profileFeignClient;
    }

    @Override
    public String getLearningStyle(String studentId) {
        try {
            log.info("Consultando profile-service para estilo de aprendizaje del studentId={}", studentId);
            String learningStyle = profileFeignClient.getLearningStyle(studentId);
            log.info("Estilo de aprendizaje obtenido del profile-service: {}", learningStyle);
            return learningStyle;
        } catch (Exception e) {
            log.warn("Profile-service no disponible para studentId={}. Usando estilo por defecto '{}'. Error: {}",
                    studentId, DEFAULT_LEARNING_STYLE, e.getMessage());
            return DEFAULT_LEARNING_STYLE;
        }
    }
}
