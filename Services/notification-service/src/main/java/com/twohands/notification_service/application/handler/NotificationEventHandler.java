package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;

public interface NotificationEventHandler {

    boolean supports(String eventType);

    NotificationEventHandlerResult handle(NotificationEvent event);
}
