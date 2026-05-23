package com.twohands.notification_service.application.worker;

import java.util.UUID;

public record ProcessNotificationEventCommand(UUID notificationEventId) {
}
