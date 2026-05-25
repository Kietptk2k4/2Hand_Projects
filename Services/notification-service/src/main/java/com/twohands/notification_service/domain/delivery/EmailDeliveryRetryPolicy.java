package com.twohands.notification_service.domain.delivery;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.email.EmailNotificationChannelPolicy;

import java.time.Instant;

public final class EmailDeliveryRetryPolicy {

    public static final int DEFAULT_MAX_RETRY_COUNT = 5;

    private EmailDeliveryRetryPolicy() {
    }

    public static boolean supportsEmailRetry(String eventType) {
        return EmailNotificationChannelPolicy.supportsEmailChannel(eventType);
    }

    public static boolean isRetryable(EmailDeliveryRetryState state) {
        if (state == null) {
            return false;
        }
        return state.failurePolicy() == NotificationFailurePolicy.RETRYABLE
                && state.retryCount() < state.maxRetryCount();
    }

    public static EmailDeliveryRetryState initialFailure(
            NotificationFailurePolicy failurePolicy,
            String lastError,
            Instant attemptedAt,
            int maxRetryCount
    ) {
        return new EmailDeliveryRetryState(
                failurePolicy,
                1,
                maxRetryCount,
                sanitizeError(lastError),
                attemptedAt
        );
    }

    public static EmailDeliveryRetryState afterRetryableFailure(
            EmailDeliveryRetryState current,
            String lastError,
            Instant attemptedAt
    ) {
        int nextRetryCount = current.retryCount() + 1;
        NotificationFailurePolicy policy = nextRetryCount >= current.maxRetryCount()
                ? NotificationFailurePolicy.PERMANENT
                : NotificationFailurePolicy.RETRYABLE;
        return new EmailDeliveryRetryState(
                policy,
                nextRetryCount,
                current.maxRetryCount(),
                sanitizeError(lastError),
                attemptedAt
        );
    }

    public static EmailDeliveryRetryState afterPermanentFailure(
            EmailDeliveryRetryState current,
            String lastError,
            Instant attemptedAt
    ) {
        return new EmailDeliveryRetryState(
                NotificationFailurePolicy.PERMANENT,
                current.maxRetryCount(),
                current.maxRetryCount(),
                sanitizeError(lastError),
                attemptedAt
        );
    }

    private static String sanitizeError(String lastError) {
        if (lastError == null || lastError.isBlank()) {
            return "Email delivery failed.";
        }
        String sanitized = lastError;
        if (sanitized.contains("verification_link") || sanitized.contains("reset_link")) {
            return "Email delivery failed.";
        }
        return sanitized.length() > 500 ? sanitized.substring(0, 500) : sanitized;
    }
}
