package com.twohands.notification_service.unit.domain.usernotification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.usernotification.DismissSystemAnnouncementNotificationPolicy;
import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DismissSystemAnnouncementNotificationPolicyTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void isSystemAnnouncement_matchesReferenceType() {
        assertTrue(DismissSystemAnnouncementNotificationPolicy.isSystemAnnouncement(
                announcement("SYSTEM_ANNOUNCEMENT", "{\"dismissible\":true}")
        ));
        assertFalse(DismissSystemAnnouncementNotificationPolicy.isSystemAnnouncement(
                announcement("POST", "{\"dismissible\":true}")
        ));
    }

    @Test
    void isDismissible_readsMetadataFlag() {
        assertTrue(DismissSystemAnnouncementNotificationPolicy.isDismissible(
                announcement("SYSTEM_ANNOUNCEMENT", "{\"dismissible\":true}"),
                objectMapper
        ));
        assertFalse(DismissSystemAnnouncementNotificationPolicy.isDismissible(
                announcement("SYSTEM_ANNOUNCEMENT", "{\"dismissible\":false}"),
                objectMapper
        ));
        assertFalse(DismissSystemAnnouncementNotificationPolicy.isDismissible(
                announcement("SYSTEM_ANNOUNCEMENT", "{}"),
                objectMapper
        ));
    }

    private UserNotification announcement(String referenceType, String metadata) {
        return new UserNotification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                "SYSTEM_ANNOUNCEMENT_SENT",
                "Title",
                "Content",
                referenceType,
                "ann-1",
                false,
                false,
                metadata,
                NotificationDeliveryStatus.SENT,
                Instant.now(),
                null
        );
    }
}
