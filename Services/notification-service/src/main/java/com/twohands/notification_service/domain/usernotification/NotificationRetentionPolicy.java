package com.twohands.notification_service.domain.usernotification;

import java.util.Set;

public final class NotificationRetentionPolicy {

    public static final String SYSTEM_ANNOUNCEMENT_REFERENCE_TYPE = "SYSTEM_ANNOUNCEMENT";

    private static final Set<String> RETAINED_NOTIFICATION_TYPES = Set.of(
            "USER_SUSPENDED",
            "USER_BANNED",
            "USER_RESTRICTED",
            "SHOP_SUSPENDED",
            "PASSWORD_CHANGED",
            "SYSTEM_ANNOUNCEMENT_SENT"
    );

    private NotificationRetentionPolicy() {
    }

    public static boolean isRetentionDaysValid(int retentionDays) {
        return retentionDays > 0;
    }

    public static Set<String> retainedNotificationTypes() {
        return RETAINED_NOTIFICATION_TYPES;
    }

    public static boolean isRetainedNotificationType(String type) {
        return type != null && RETAINED_NOTIFICATION_TYPES.contains(type);
    }

    public static boolean isRetainedReferenceType(String referenceType) {
        return SYSTEM_ANNOUNCEMENT_REFERENCE_TYPE.equals(referenceType);
    }
}
