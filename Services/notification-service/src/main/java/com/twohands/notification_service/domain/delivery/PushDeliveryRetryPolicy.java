package com.twohands.notification_service.domain.delivery;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.push.PushNotificationChannelPolicy;

import java.time.Instant;

public final class PushDeliveryRetryPolicy {

    public static final int DEFAULT_MAX_RETRY_COUNT = 5;

    private PushDeliveryRetryPolicy() {
    }

    public static boolean supportsPushRetry(String eventType) {
        return PushNotificationChannelPolicy.supportsPushChannel(eventType);
    }

    public static boolean isRetryable(PushDeliveryRetryState state) {
        if (state == null) {
            return false;
        }
        return state.failurePolicy() == NotificationFailurePolicy.RETRYABLE
                && state.retryCount() < state.maxRetryCount();
    }

    public static PushDeliveryRetryState initialFailure(
            NotificationFailurePolicy failurePolicy,
            String lastError,
            Instant attemptedAt,
            int maxRetryCount
    ) {
        return new PushDeliveryRetryState(
                failurePolicy,
                1,
                maxRetryCount,
                sanitizeError(lastError),
                attemptedAt
        );
    }

    public static PushDeliveryRetryState afterRetryableFailure(
            PushDeliveryRetryState current,
            String lastError,
            Instant attemptedAt
    ) {
        int nextRetryCount = current.retryCount() + 1;
        NotificationFailurePolicy policy = nextRetryCount >= current.maxRetryCount()
                ? NotificationFailurePolicy.PERMANENT
                : NotificationFailurePolicy.RETRYABLE;
        return new PushDeliveryRetryState(
                policy,
                nextRetryCount,
                current.maxRetryCount(),
                sanitizeError(lastError),
                attemptedAt
        );
    }

    public static PushDeliveryRetryState afterPermanentFailure(
            PushDeliveryRetryState current,
            String lastError,
            Instant attemptedAt
    ) {
        return new PushDeliveryRetryState(
                NotificationFailurePolicy.PERMANENT,
                current.maxRetryCount(),
                current.maxRetryCount(),
                sanitizeError(lastError),
                attemptedAt
        );
    }

    private static String sanitizeError(String lastError) {
        if (lastError == null || lastError.isBlank()) {
            return "Push delivery failed.";
        }
        return lastError.length() > 500 ? lastError.substring(0, 500) : lastError;
    }
}
