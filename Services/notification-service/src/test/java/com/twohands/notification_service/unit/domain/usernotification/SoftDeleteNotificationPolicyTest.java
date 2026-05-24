package com.twohands.notification_service.unit.domain.usernotification;

import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.SoftDeleteNotificationPolicy;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoftDeleteNotificationPolicyTest {

    @Test
    void apply_softDeletesVisibleNotification() {
        UserNotification notification = notification(false);

        var outcome = SoftDeleteNotificationPolicy.apply(notification);

        assertTrue(outcome.changed());
        assertTrue(outcome.notification().deleted());
    }

    @Test
    void apply_isIdempotentWhenAlreadyDeleted() {
        UserNotification notification = notification(true);

        var outcome = SoftDeleteNotificationPolicy.apply(notification);

        assertFalse(outcome.changed());
        assertTrue(outcome.notification().deleted());
    }

    private UserNotification notification(boolean deleted) {
        return new UserNotification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "POST_LIKED",
                "Title",
                "Content",
                "POST",
                "post-1",
                false,
                deleted,
                "{}",
                NotificationDeliveryStatus.SENT,
                Instant.now(),
                null
        );
    }
}
