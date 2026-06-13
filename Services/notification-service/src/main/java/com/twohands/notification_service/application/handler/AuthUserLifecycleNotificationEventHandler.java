package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Acknowledges Auth user lifecycle events that Notification does not surface to users.
 * Social Service owns profile projection sync for {@code USER_UPDATED} / {@code USER_DELETED}.
 */
@Component
@Order(5)
public class AuthUserLifecycleNotificationEventHandler implements NotificationEventHandler {

    private static final Set<String> SUPPORTED_EVENT_TYPES = Set.of(
            "USER_UPDATED",
            "USER_DELETED"
    );

    @Override
    public boolean supports(String eventType) {
        return SUPPORTED_EVENT_TYPES.contains(eventType);
    }

    @Override
    public NotificationEventHandlerResult handle(NotificationEvent event) {
        return NotificationEventHandlerResult.noOp();
    }
}
