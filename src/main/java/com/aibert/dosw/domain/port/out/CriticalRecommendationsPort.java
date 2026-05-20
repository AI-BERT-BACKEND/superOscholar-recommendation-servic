package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.TaskDTO;
import com.aibert.dosw.domain.model.CriticalRecommendationsResult;

import java.util.List;

/**
 * Puerto de salida para obtener recomendaciones críticas del
 * engineplanning-service.
 * Modo A: orderedTasks = null → el motor calcula las tareas críticas solo.
 * Modo B: orderedTasks = lista → se usa la lista precalculada de tareas.
 */
public interface CriticalRecommendationsPort {

    /**
     * @param studentId    ID del estudiante (va en header X-Student-Id)
     * @param orderedTasks lista de tareas precalculadas; null para que el motor
     *                     calcule
     * @return resultado con la lista de tareas críticas y metadata
     */
    CriticalRecommendationsResult getCriticalRecommendations(String studentId, List<TaskDTO> orderedTasks);
}
