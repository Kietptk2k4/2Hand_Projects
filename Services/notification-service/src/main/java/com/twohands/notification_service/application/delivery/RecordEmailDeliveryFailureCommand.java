package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;

import java.util.UUID;

public record RecordEmailDeliveryFailureCommand(
        UUID userNotificationId,
        NotificationFailurePolicy failurePolicy,
        String failureReason
) {
}
