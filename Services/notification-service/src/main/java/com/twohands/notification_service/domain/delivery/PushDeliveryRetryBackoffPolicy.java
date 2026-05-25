package com.twohands.notification_service.domain.delivery;

import com.twohands.notification_service.domain.notificationevent.NotificationEventRetryBackoffPolicy;

import java.time.Instant;

public final class PushDeliveryRetryBackoffPolicy {

    private PushDeliveryRetryBackoffPolicy() {
    }

    public static long computeBackoffSeconds(int retryCount, int baseBackoffSeconds, int maxBackoffSeconds) {
        return NotificationEventRetryBackoffPolicy.computeBackoffSeconds(
                retryCount,
                baseBackoffSeconds,
                maxBackoffSeconds
        );
    }

    public static boolean isEligibleForRetry(
            PushDeliveryRetryState state,
            Instant now,
            int baseBackoffSeconds,
            int maxBackoffSeconds
    ) {
        if (!PushDeliveryRetryPolicy.isRetryable(state)) {
            return false;
        }
        if (state.lastAttemptAt() == null) {
            return true;
        }
        long backoffSeconds = computeBackoffSeconds(state.retryCount(), baseBackoffSeconds, maxBackoffSeconds);
        return !state.lastAttemptAt().plusSeconds(backoffSeconds).isAfter(now);
    }
}
