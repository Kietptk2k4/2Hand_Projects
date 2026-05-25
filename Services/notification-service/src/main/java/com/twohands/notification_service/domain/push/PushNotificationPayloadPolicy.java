package com.twohands.notification_service.domain.push;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class PushNotificationPayloadPolicy {

    private static final int MAX_TITLE_LENGTH = 120;
    private static final int MAX_BODY_LENGTH = 240;
    private static final int MAX_DATA_VALUE_LENGTH = 200;

    private PushNotificationPayloadPolicy() {
    }

    public static PushNotificationPayload build(
            PushNotificationTemplate template,
            String eventType,
            String referenceType,
            String referenceId,
            UUID notificationEventId
    ) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("eventType", sanitizeValue(eventType));

        if (referenceType != null && !referenceType.isBlank()) {
            data.put("referenceType", sanitizeValue(referenceType));
        }
        if (referenceId != null && !referenceId.isBlank()) {
            data.put("referenceId", sanitizeValue(referenceId));
        }
        if (notificationEventId != null) {
            data.put("notificationEventId", notificationEventId.toString());
        }

        return new PushNotificationPayload(
                truncate(template.title(), MAX_TITLE_LENGTH),
                truncate(template.body(), MAX_BODY_LENGTH),
                Map.copyOf(data)
        );
    }

    private static String sanitizeValue(String value) {
        return truncate(value.trim(), MAX_DATA_VALUE_LENGTH);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
