package com.twohands.notification_service.delivery.http.internal.request;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IngestNotificationEventRequest(
        UUID sourceEventId,
        String eventKey,
        @NotBlank String eventType,
        @NotNull NotificationSourceService sourceService,
        String aggregateType,
        String aggregateId,
        UUID actorId,
        UUID recipientUserId,
        String payload
) {
}
