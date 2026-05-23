package com.twohands.notification_service.application.consume;

import java.util.UUID;

public record ConsumeDomainEventResult(
        UUID notificationEventId,
        boolean duplicate
) {
}
