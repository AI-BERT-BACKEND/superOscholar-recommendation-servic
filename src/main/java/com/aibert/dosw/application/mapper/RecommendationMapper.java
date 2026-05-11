package com.aibert.dosw.application.mapper;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    DailyRecommendationDTO toDailyRecommendationDto(Recommendation recommendation);
}
