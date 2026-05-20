package com.aibert.dosw.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private Long userId;

    private NotificationType type;

    private String title;

    private String message;

    private NotificationSeverity severity;

    private Long relatedEntityId;
}
