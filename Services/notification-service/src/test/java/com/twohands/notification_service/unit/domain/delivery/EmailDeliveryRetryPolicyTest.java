package com.twohands.notification_service.unit.domain.delivery;

import com.twohands.notification_service.application.worker.NotificationFailurePolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryPolicy;
import com.twohands.notification_service.domain.delivery.EmailDeliveryRetryState;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailDeliveryRetryPolicyTest {

    @Test
    void isRetryable_returnsTrueForRetryableUnderMax() {
        var state = new EmailDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                2,
                5,
                "Email provider timeout.",
                Instant.parse("2026-05-20T08:00:00Z")
        );

        assertTrue(EmailDeliveryRetryPolicy.isRetryable(state));
    }

    @Test
    void sanitizeError_doesNotExposeSensitiveLinks() {
        var next = EmailDeliveryRetryPolicy.afterRetryableFailure(
                EmailDeliveryRetryPolicy.initialFailure(
                        NotificationFailurePolicy.RETRYABLE,
                        "Missing verification_link in payload",
                        Instant.parse("2026-05-20T08:00:00Z"),
                        5
                ),
                "reset_link expired",
                Instant.parse("2026-05-20T09:00:00Z")
        );

        assertEquals("Email delivery failed.", next.lastError());
    }

    @Test
    void afterRetryableFailure_escalatesToPermanentAtMax() {
        var current = new EmailDeliveryRetryState(
                NotificationFailurePolicy.RETRYABLE,
                4,
                5,
                "old",
                Instant.parse("2026-05-20T08:00:00Z")
        );

        var next = EmailDeliveryRetryPolicy.afterRetryableFailure(
                current,
                "Email provider timeout.",
                Instant.parse("2026-05-20T09:00:00Z")
        );

        assertEquals(5, next.retryCount());
        assertEquals(NotificationFailurePolicy.PERMANENT, next.failurePolicy());
        assertFalse(EmailDeliveryRetryPolicy.isRetryable(next));
    }
}
