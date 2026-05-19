package com.aibert.dosw.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    @NotBlank(message = "El studentId es obligatorio")
    private String studentId;

    /**
     * R18: Tipo de recomendación solicitado.
     * Valores válidos: PRODUCTIVIDAD, CARGA, GENERAL (case-insensitive).
     * Si es null o blank, se genera GENERAL por defecto.
     */
    @Pattern(regexp = "^$|(?i)PRODUCTIVIDAD|CARGA|GENERAL", message = "requestType debe ser PRODUCTIVIDAD, CARGA o GENERAL")
    private String requestType;

    /**
     * Contexto enriquecido que puede enviar el engineplaning-service.
     * Si está presente, se usa directamente para construir el prompt de IA
     * evitando una llamada circular de vuelta al planning-service.
     * Campo opcional: si es null, el servicio consulta el planning-service.
     */
    @Valid
    private RecommendationContextDTO context;
}
