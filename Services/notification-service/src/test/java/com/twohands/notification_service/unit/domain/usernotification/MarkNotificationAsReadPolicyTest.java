package com.twohands.notification_service.unit.domain.usernotification;

import com.twohands.notification_service.domain.usernotification.MarkNotificationAsReadPolicy;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarkNotificationAsReadPolicyTest {

    @Test
    void apply_marksUnreadNotificationAsRead() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-24T10:00:00Z");
        Instant readAt = Instant.parse("2026-05-24T12:00:00Z");
        UserNotification unread = notification(id, false, null, createdAt);

        var outcome = MarkNotificationAsReadPolicy.apply(unread, readAt);

        assertTrue(outcome.changed());
        assertTrue(outcome.notification().read());
        assertEquals(readAt, outcome.notification().readAt());
    }

    @Test
    void apply_isIdempotentWhenAlreadyRead() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-24T10:00:00Z");
        Instant existingReadAt = Instant.parse("2026-05-24T11:00:00Z");
        UserNotification read = notification(id, true, existingReadAt, createdAt);

        var outcome = MarkNotificationAsReadPolicy.apply(read, Instant.parse("2026-05-24T12:00:00Z"));

        assertFalse(outcome.changed());
        assertEquals(existingReadAt, outcome.notification().readAt());
    }

    private UserNotification notification(UUID id, boolean read, Instant readAt, Instant createdAt) {
        return new UserNotification(
                id,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "POST_LIKED",
                "Title",
                "Content",
                "POST",
                "post-1",
                read,
                false,
                "{}",
                NotificationDeliveryStatus.SENT,
                createdAt,
                readAt
        );
    }
}
