package com.aibert.dosw.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    @NotNull(message = "El studentId es obligatorio")
    private Long studentId;

    /**
     * R18: Tipo de recomendación solicitado.
     * Valores válidos: PRODUCTIVIDAD, CARGA, GENERAL.
     * Si es null o no se envía, se genera GENERAL por defecto.
     */
    private String requestType;
}
