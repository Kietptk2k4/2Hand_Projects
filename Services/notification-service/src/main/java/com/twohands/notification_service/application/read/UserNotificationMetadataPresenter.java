package com.twohands.notification_service.application.read;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.notification_service.domain.admin.SystemAnnouncementNotificationMetadataPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventPayloadSanitizer;

public final class UserNotificationMetadataPresenter {

    private UserNotificationMetadataPresenter() {
    }

    public static String present(
            ObjectMapper objectMapper,
            NotificationEventPayloadSanitizer metadataSanitizer,
            String referenceType,
            String metadata
    ) {
        String sanitized = metadataSanitizer.sanitize(metadata);
        if ("SYSTEM_ANNOUNCEMENT".equals(referenceType)) {
            return SystemAnnouncementNotificationMetadataPolicy.sanitizeForResponse(objectMapper, sanitized);
        }
        return sanitized;
    }
}
