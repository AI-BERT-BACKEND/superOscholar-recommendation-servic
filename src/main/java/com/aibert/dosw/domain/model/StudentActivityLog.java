package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "student_activity_logs")
public class StudentActivityLog {
    @Id
    private String id;

    private String studentId;

    private Long taskId; // ID de la tarea proveniente del task-service

    private String subject; // Nombre de la materia (ej. Matemáticas, Programación)

    private String activityType; // Ej: "TASK_COMPLETED", "TASK_FAILED", "TASK_RESCHEDULED"

    private LocalDateTime scheduledStartTime; // Para analizar patrones de horario (ej. le cuesta a las 8 AM)

    private LocalDateTime actualCompletionTime; // Cuando realmente se completó/falló

    private Integer focusScore; // Métrica opcional (1-100) sobre qué tan concentrado estuvo

    private String userFeedback; // Comentario opcional del estudiante sobre la tarea

    private LocalDateTime logDate; // Cuándo se registró este log en el sistema
}
