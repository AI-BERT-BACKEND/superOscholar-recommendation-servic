package com.aibert.dosw.infrastructure.adapters.out.feign;

import com.aibert.dosw.domain.port.out.ProfileServicePort;
import org.springframework.stereotype.Component;

@Component
public class ProfileFeignClientAdapter implements ProfileServicePort {
    @Override
    public String getLearningStyle(Long studentId) {
        return "VISUAL"; // Mock por ahora
    }
}
