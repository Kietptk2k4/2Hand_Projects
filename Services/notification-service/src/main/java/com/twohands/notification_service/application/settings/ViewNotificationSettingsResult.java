package com.twohands.notification_service.application.settings;

import com.twohands.notification_service.domain.notificationsetting.EffectiveNotificationSetting;

import java.util.List;
import java.util.UUID;

public record ViewNotificationSettingsResult(
        UUID userId,
        List<EffectiveNotificationSetting> settings
) {
}
