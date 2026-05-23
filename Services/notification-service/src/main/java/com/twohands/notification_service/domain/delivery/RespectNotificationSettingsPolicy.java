package com.twohands.notification_service.domain.delivery;

import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;

import java.util.Optional;

public final class RespectNotificationSettingsPolicy {

    private RespectNotificationSettingsPolicy() {
    }

    public static NotificationChannelPreferences resolve(
            Optional<UserNotificationSetting> userSetting,
            DefaultChannelFlags defaults
    ) {
        boolean inApp = userSetting.map(UserNotificationSetting::allowInApp).orElse(defaults.inApp());
        boolean push = userSetting.map(UserNotificationSetting::allowPush).orElse(defaults.push());
        boolean email = userSetting.map(UserNotificationSetting::allowEmail).orElse(defaults.email());
        return new NotificationChannelPreferences(inApp, push, email);
    }
}
