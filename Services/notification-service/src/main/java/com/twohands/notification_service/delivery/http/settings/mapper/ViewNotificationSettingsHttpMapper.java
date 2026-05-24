package com.twohands.notification_service.delivery.http.settings.mapper;

import com.twohands.notification_service.application.settings.ViewNotificationSettingsResult;
import com.twohands.notification_service.delivery.http.settings.response.NotificationSettingItemResponse;
import com.twohands.notification_service.delivery.http.settings.response.ViewNotificationSettingsResponse;
import com.twohands.notification_service.domain.notificationsetting.EffectiveNotificationSetting;
import org.springframework.stereotype.Component;

@Component
public class ViewNotificationSettingsHttpMapper {

    public ViewNotificationSettingsResponse toResponse(ViewNotificationSettingsResult result) {
        return new ViewNotificationSettingsResponse(
                result.settings().stream()
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    private NotificationSettingItemResponse toItemResponse(EffectiveNotificationSetting setting) {
        return new NotificationSettingItemResponse(
                setting.eventType(),
                setting.allowPush(),
                setting.allowEmail(),
                setting.allowInApp(),
                setting.explicitSetting()
        );
    }
}
