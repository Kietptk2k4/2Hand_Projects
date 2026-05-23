package com.twohands.notification_service.unit.domain.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRetryBackoffPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationEventRetryBackoffPolicyTest {

    @Test
    void computeBackoffSeconds_usesExponentialDelayWithCap() {
        assertEquals(30, NotificationEventRetryBackoffPolicy.computeBackoffSeconds(1, 30, 3600));
        assertEquals(60, NotificationEventRetryBackoffPolicy.computeBackoffSeconds(2, 30, 3600));
        assertEquals(120, NotificationEventRetryBackoffPolicy.computeBackoffSeconds(3, 30, 3600));
        assertEquals(3600, NotificationEventRetryBackoffPolicy.computeBackoffSeconds(20, 30, 3600));
    }

    @Test
    void isEligibleForRetry_allowsImmediateRetryWhenFailureTimeMissing() {
        NotificationEvent event = failedEvent(1, null);
        assertTrue(NotificationEventRetryBackoffPolicy.isEligibleForRetry(
                event,
                Instant.now(),
                30,
                3600
        ));
    }

    @Test
    void isEligibleForRetry_respectsBackoffWindow() {
        Instant failedAt = Instant.now().minusSeconds(20);
        NotificationEvent event = failedEvent(1, failedAt);

        assertFalse(NotificationEventRetryBackoffPolicy.isEligibleForRetry(
                event,
                Instant.now(),
                30,
                3600
        ));

        assertTrue(NotificationEventRetryBackoffPolicy.isEligibleForRetry(
                event,
                failedAt.plusSeconds(31),
                30,
                3600
        ));
    }

    @Test
    void isEligibleForRetry_rejectsPermanentFailure() {
        NotificationEvent event = new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}",
                NotificationEventStatus.FAILED,
                5,
                5,
                "permanent",
                Instant.now().minusSeconds(3600),
                null,
                Instant.now(),
                null
        );

        assertFalse(NotificationEventRetryBackoffPolicy.isEligibleForRetry(
                event,
                Instant.now(),
                30,
                3600
        ));
    }

    private NotificationEvent failedEvent(int retryCount, Instant failedAt) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                null,
                null,
                null,
                null,
                "{}",
                NotificationEventStatus.FAILED,
                retryCount,
                5,
                "retryable",
                failedAt,
                null,
                Instant.now(),
                null
        );
    }
}
