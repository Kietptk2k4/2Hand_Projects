package com.twohands.notification_service.domain.delivery;

import java.time.Instant;

public final class EmailDeliveryRetryBackoffPolicy {

    private EmailDeliveryRetryBackoffPolicy() {
    }

    public static long computeBackoffSeconds(int retryCount, int baseBackoffSeconds, int maxBackoffSeconds) {
        return PushDeliveryRetryBackoffPolicy.computeBackoffSeconds(
                retryCount,
                baseBackoffSeconds,
                maxBackoffSeconds
        );
    }

    public static boolean isEligibleForRetry(
            EmailDeliveryRetryState state,
            Instant now,
            int baseBackoffSeconds,
            int maxBackoffSeconds
    ) {
        if (!EmailDeliveryRetryPolicy.isRetryable(state)) {
            return false;
        }
        if (state.lastAttemptAt() == null) {
            return true;
        }
        long backoffSeconds = computeBackoffSeconds(state.retryCount(), baseBackoffSeconds, maxBackoffSeconds);
        return !state.lastAttemptAt().plusSeconds(backoffSeconds).isAfter(now);
    }
}
