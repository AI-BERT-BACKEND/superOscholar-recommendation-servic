package com.aibert.dosw.infrastructure.adapters.in.rest.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DailyRecommendationDTO {
    private Long studentId;
    private String motivationalMessage;
    private List<String> studyTips;
    private LocalDate dateGenerated;
    private Double confidenceScore;
}
