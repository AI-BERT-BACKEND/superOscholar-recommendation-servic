package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Resultado de dominio con las recomendaciones críticas devueltas por el engineplanning-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriticalRecommendationsResult {
    private List<TaskDTO> criticalTasks;
    private int criticalCount;
    private String message;
    private boolean availableFromService;
}
