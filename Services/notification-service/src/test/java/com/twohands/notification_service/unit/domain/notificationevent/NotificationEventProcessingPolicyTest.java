package com.twohands.notification_service.unit.domain.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventProcessingPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationEventProcessingPolicyTest {

    @Test
    void completedEventIsTerminalAndSkippedForDuplicateProcessing() {
        NotificationEvent completed = sampleEvent(NotificationEventStatus.COMPLETED, 0);

        assertTrue(NotificationEventProcessingPolicy.isTerminal(NotificationEventStatus.COMPLETED));
        assertTrue(NotificationEventProcessingPolicy.shouldSkipDuplicateProcessing(completed));
        assertFalse(NotificationEventProcessingPolicy.canProcess(completed));
    }

    @Test
    void failedEventIsRetryableUntilMaxRetries() {
        NotificationEvent retryable = sampleEvent(NotificationEventStatus.FAILED, 2);
        NotificationEvent permanent = sampleEvent(NotificationEventStatus.FAILED, 5);

        assertTrue(NotificationEventProcessingPolicy.isRetryable(retryable));
        assertTrue(NotificationEventProcessingPolicy.canProcess(retryable));
        assertTrue(NotificationEventProcessingPolicy.isPermanentFailure(permanent));
        assertFalse(NotificationEventProcessingPolicy.canProcess(permanent));
    }

    private NotificationEvent sampleEvent(NotificationEventStatus status, int retryCount) {
        return new NotificationEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "POST_LIKED",
                NotificationSourceService.SOCIAL,
                "POST",
                "post-id",
                null,
                null,
                "{}",
                status,
                retryCount,
                5,
                null,
                null,
                null,
                Instant.now(),
                status == NotificationEventStatus.COMPLETED ? Instant.now() : null
        );
    }
}
