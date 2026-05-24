package com.twohands.notification_service.delivery.http.settings.response;

public record NotificationSettingItemResponse(
        String eventType,
        boolean allowPush,
        boolean allowEmail,
        boolean allowInApp,
        boolean explicitSetting
) {
}
