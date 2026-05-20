package com.aibert.dosw.infrastructure.adapters.out.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriticalRecommendationsRequest {
    private List<CriticalTaskCandidateRequest> orderedTasks; // null = el motor calcula solo
}
