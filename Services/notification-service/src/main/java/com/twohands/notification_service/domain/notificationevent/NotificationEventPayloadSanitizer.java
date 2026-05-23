package com.twohands.notification_service.domain.notificationevent;

public interface NotificationEventPayloadSanitizer {

    /**
     * Validates JSON and redacts sensitive fields before persisting notification event payload.
     */
    String sanitize(String rawPayload);
}
