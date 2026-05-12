package com.aibert.dosw.application.mapper;

import com.aibert.dosw.domain.model.Recommendation;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.DailyRecommendationDTO;
import com.aibert.dosw.infrastructure.adapters.in.rest.dto.RecommendationItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    
    @Mapping(target = "message", ignore = true) // El mensaje se asignará en el UseCase
    DailyRecommendationDTO toDailyRecommendationDto(Recommendation recommendation);

    default List<RecommendationItemDTO> mapItems(List<Recommendation.RecommendationItem> items) {
        if (items == null) return null;
        return items.stream().map(item -> RecommendationItemDTO.builder()
                .title(item.getTitle())
                .description(item.getDescription())
                .recommendationType(item.getType())
                .confidenceScore(item.getItemScore())
                .build()).collect(Collectors.toList());
    }
}
