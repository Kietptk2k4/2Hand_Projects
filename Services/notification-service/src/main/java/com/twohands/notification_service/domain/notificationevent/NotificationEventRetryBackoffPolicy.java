package com.twohands.notification_service.domain.notificationevent;

import java.time.Instant;

public final class NotificationEventRetryBackoffPolicy {

    private static final int MAX_EXPONENT = 10;

    private NotificationEventRetryBackoffPolicy() {
    }

    public static long computeBackoffSeconds(int retryCount, int baseBackoffSeconds, int maxBackoffSeconds) {
        if (baseBackoffSeconds <= 0) {
            return 0;
        }
        int normalizedRetryCount = Math.max(retryCount, 1);
        int exponent = Math.min(normalizedRetryCount - 1, MAX_EXPONENT);
        long delay = (long) baseBackoffSeconds * (1L << exponent);
        return Math.min(delay, maxBackoffSeconds);
    }

    public static boolean isEligibleForRetry(
            NotificationEvent event,
            Instant now,
            int baseBackoffSeconds,
            int maxBackoffSeconds
    ) {
        if (event.status() != NotificationEventStatus.FAILED
                || !NotificationEventProcessingPolicy.isRetryable(event)) {
            return false;
        }
        if (event.lockedAt() == null) {
            return true;
        }
        long backoffSeconds = computeBackoffSeconds(event.retryCount(), baseBackoffSeconds, maxBackoffSeconds);
        return !event.lockedAt().plusSeconds(backoffSeconds).isAfter(now);
    }
}
