package com.aibert.dosw.application.mapper;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.RecommendationItemDTO;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecommendationMapperTest {

    private final RecommendationMapper mapper = Mappers.getMapper(RecommendationMapper.class);

    @Test
    void mapItems_withNullInput_returnsNull() {
        assertNull(mapper.mapItems(null));
    }

    @Test
    void mapItems_withValues_mapsFields() {
        Recommendation.RecommendationItem item = Recommendation.RecommendationItem.builder()
                .title("Prioriza calculo")
                .description("Enfocate en ejercicios tipo parcial")
                .type("ACADEMIC")
                .itemScore(0.9)
                .build();

        List<RecommendationItemDTO> result = mapper.mapItems(List.of(item));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Prioriza calculo", result.get(0).getTitle());
        assertEquals("Enfocate en ejercicios tipo parcial", result.get(0).getRecommendation());
        assertEquals("ACADEMIC", result.get(0).getType());
        assertEquals(0.9, result.get(0).getConfidenceScore());
    }

    @Test
    void toDailyRecommendationDto_mapsTopLevelAndItems() {
        Recommendation recommendation = Recommendation.builder()
                .studentId("42")
                .confidenceScore(0.87)
                .recommendationType("GENERAL")
                .motivationalMessage("Sigue así")
                .dateGenerated(LocalDate.of(2026, 5, 20))
                .recommendations(List.of(Recommendation.RecommendationItem.builder()
                        .title("Revisa tus apuntes")
                        .description("Dedica 30 minutos al repaso activo")
                        .type("GENERAL")
                        .itemScore(0.81)
                        .build()))
                .build();

        DailyRecommendationDTO dto = mapper.toDailyRecommendationDto(recommendation);

        assertEquals("42", dto.getStudentId());
        assertEquals(0.87, dto.getConfidenceScore());
        assertEquals("GENERAL", dto.getRecommendationType());
        assertEquals("Sigue así", dto.getMotivationalMessage());
        assertEquals(LocalDate.of(2026, 5, 20), dto.getDateGenerated());
        assertNull(dto.getMessage());
        assertNotNull(dto.getRecommendations());
        assertEquals(1, dto.getRecommendations().size());
        assertEquals("Revisa tus apuntes", dto.getRecommendations().get(0).getTitle());
    }
}
