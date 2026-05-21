package com.aibert.dosw.infrastructure.adapters.out.feign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileFeignClientAdapterTest {

    @Mock
    private ProfileFeignClient profileFeignClient;

    @InjectMocks
    private ProfileFeignClientAdapter adapter;

    @Test
    void getLearningStyle_whenServiceAvailable_returnsStyleFromClient() {
        when(profileFeignClient.getLearningStyle("student-1")).thenReturn("KINESTHETIC");

        String result = adapter.getLearningStyle("student-1");

        assertEquals("KINESTHETIC", result);
        verify(profileFeignClient).getLearningStyle("student-1");
    }

    @Test
    void getLearningStyle_whenClientReturnsAuditory_returnsThatValue() {
        when(profileFeignClient.getLearningStyle("student-2")).thenReturn("AUDITORY");

        assertEquals("AUDITORY", adapter.getLearningStyle("student-2"));
    }

    @Test
    void getLearningStyle_whenClientThrowsException_returnsDefaultVisual() {
        when(profileFeignClient.getLearningStyle("student-3"))
                .thenThrow(new RuntimeException("profile-service down"));

        String result = adapter.getLearningStyle("student-3");

        assertEquals("VISUAL", result);
    }

    @Test
    void getLearningStyle_whenClientThrowsFeignException_returnsDefaultVisual() {
        when(profileFeignClient.getLearningStyle("student-4"))
                .thenThrow(new feign.FeignException.ServiceUnavailable(
                        "unavailable",
                        feign.Request.create(feign.Request.HttpMethod.GET, "http://profile/api/v1/profiles/student-4/learning-style",
                                java.util.Collections.emptyMap(), null, new feign.RequestTemplate()),
                        null, null));

        String result = adapter.getLearningStyle("student-4");

        assertEquals("VISUAL", result);
    }
}
