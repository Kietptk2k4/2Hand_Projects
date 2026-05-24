package com.twohands.notification_service.delivery.http.settings.response;

import java.util.List;

public record ViewNotificationSettingsResponse(
        List<NotificationSettingItemResponse> settings
) {
}
