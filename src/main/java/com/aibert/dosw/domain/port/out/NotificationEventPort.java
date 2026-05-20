package com.aibert.dosw.domain.port.out;

import com.aibert.dosw.domain.model.NotificationEvent;

public interface NotificationEventPort {

    void sendNotification(NotificationEvent event);
}
