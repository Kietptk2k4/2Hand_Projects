package com.twohands.notification_service.unit.domain.delivery;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.PushDeliveryRetryState;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PushDeliveryRetryPolicyTest {

    @Test
    void isRetryable_returnsTrueForRetryableUnderMax() {
        var state = new PushDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                2,
                5,
                "FCM timeout",
                Instant.parse("2026-05-20T08:00:00Z")
        );

        assertTrue(PushDeliveryRetryPolicy.isRetryable(state));
    }

    @Test
    void isRetryable_returnsFalseWhenMaxExceeded() {
        var state = new PushDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                5,
                5,
                "FCM timeout",
                Instant.parse("2026-05-20T08:00:00Z")
        );

        assertFalse(PushDeliveryRetryPolicy.isRetryable(state));
    }

    @Test
    void afterRetryableFailure_incrementsCountAndEscalatesAtMax() {
        var current = new PushDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                4,
                5,
                "old",
                Instant.parse("2026-05-20T08:00:00Z")
        );

        var next = PushDeliveryRetryPolicy.afterRetryableFailure(
                current,
                "FCM timeout",
                Instant.parse("2026-05-20T09:00:00Z")
        );

        assertEquals(5, next.retryCount());
        assertEquals(NotificationFailurePolicy.PERMANENT, next.failurePolicy());
    }
}
