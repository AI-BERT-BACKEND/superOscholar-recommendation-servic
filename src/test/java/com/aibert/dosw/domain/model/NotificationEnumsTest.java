package com.aibert.dosw.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationEnumsTest {

    @Test
    void notificationSeverity_valuesAndValueOf_areConsistent() {
        assertEquals(4, NotificationSeverity.values().length);
        assertEquals(NotificationSeverity.LOW, NotificationSeverity.valueOf("LOW"));
        assertEquals(NotificationSeverity.MEDIUM, NotificationSeverity.valueOf("MEDIUM"));
        assertEquals(NotificationSeverity.HIGH, NotificationSeverity.valueOf("HIGH"));
        assertEquals(NotificationSeverity.CRITICAL, NotificationSeverity.valueOf("CRITICAL"));
    }

    @Test
    void notificationType_valuesAndValueOf_areConsistent() {
        assertEquals(2, NotificationType.values().length);
        assertEquals(NotificationType.STUDY_SUGGESTION, NotificationType.valueOf("STUDY_SUGGESTION"));
        assertEquals(NotificationType.OVERLOAD_ALERT, NotificationType.valueOf("OVERLOAD_ALERT"));
    }
}
