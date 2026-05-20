package com.aibert.dosw.infrastructure.adapters.out.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriticalRecommendationsResponse {
    private List<PrioritizedTaskResponse> criticalRecommendations;
    private Integer criticalCount;
    private String message;
}
