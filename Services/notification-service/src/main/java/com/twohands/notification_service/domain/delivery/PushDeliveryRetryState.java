package com.twohands.notification_service.domain.delivery;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;

import java.time.Instant;

public record PushDeliveryRetryState(
        NotificationFailurePolicy failurePolicy,
        int retryCount,
        int maxRetryCount,
        String lastError,
        Instant lastAttemptAt
) {
}
