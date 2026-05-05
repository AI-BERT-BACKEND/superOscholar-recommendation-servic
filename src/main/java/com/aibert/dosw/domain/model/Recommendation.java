package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommendations")
public class Recommendation {
    @Id
    private String id;
    private Long studentId;
    private String motivationalMessage;
    private List<String> studyTips;
    private LocalDate dateGenerated;
    private String userFeedback; // "LIKE", "DISLIKE", null
    private Double confidenceScore; // Nivel de confianza de la IA en su recomendación
}
