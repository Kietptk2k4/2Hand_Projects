package com.twohands.notification_service.domain.usernotification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.SystemAnnouncementDismissibleMetadataPolicy;

public final class DismissSystemAnnouncementNotificationPolicy {

    public static final String SYSTEM_ANNOUNCEMENT_REFERENCE_TYPE = "SYSTEM_ANNOUNCEMENT";

    private DismissSystemAnnouncementNotificationPolicy() {
    }

    public static boolean isSystemAnnouncement(UserNotification notification) {
        return notification != null
                && SYSTEM_ANNOUNCEMENT_REFERENCE_TYPE.equals(notification.referenceType());
    }

    public static boolean isDismissible(UserNotification notification, ObjectMapper objectMapper) {
        if (notification == null) {
            return false;
        }
        return SystemAnnouncementDismissibleMetadataPolicy.isDismissibleFromStoredMetadata(
                objectMapper,
                notification.metadata()
        );
    }
}
