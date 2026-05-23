package com.twohands.notification_service.domain.idempotency;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;

import java.util.UUID;

public record NotificationEventIdempotencyKey(
        NotificationSourceService sourceService,
        UUID sourceEventId,
        String eventKey
) {

    public boolean hasSourceEventId() {
        return sourceEventId != null;
    }

    public boolean hasEventKey() {
        return eventKey != null && !eventKey.isBlank();
    }

    public boolean isPresent() {
        return hasSourceEventId() || hasEventKey();
    }

    public String normalizedEventKey() {
        if (eventKey == null || eventKey.isBlank()) {
            return null;
        }
        return eventKey.trim();
    }
}
