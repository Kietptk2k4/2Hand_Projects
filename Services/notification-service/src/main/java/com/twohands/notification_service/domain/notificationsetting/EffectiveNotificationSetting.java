package com.twohands.notification_service.domain.notificationsetting;

public record EffectiveNotificationSetting(
        String eventType,
        boolean allowPush,
        boolean allowEmail,
        boolean allowInApp,
        boolean explicitSetting
) {
}
