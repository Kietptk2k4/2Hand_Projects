package com.twohands.notification_service.domain.notificationsetting;

import com.twohands.notification_service.domain.delivery.DefaultChannelFlags;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class ViewNotificationSettingsPolicy {

    private ViewNotificationSettingsPolicy() {
    }

    public static List<EffectiveNotificationSetting> resolve(
            Map<String, UserNotificationSetting> explicitSettingsByEventType
    ) {
        List<String> eventTypes = new ArrayList<>(NotificationDefaultChannelPolicy.supportedEventTypes());
        eventTypes.sort(Comparator.naturalOrder());

        return eventTypes.stream()
                .map(eventType -> resolveForEventType(eventType, explicitSettingsByEventType.get(eventType)))
                .toList();
    }

    private static EffectiveNotificationSetting resolveForEventType(
            String eventType,
            UserNotificationSetting explicitSetting
    ) {
        if (explicitSetting != null) {
            return new EffectiveNotificationSetting(
                    eventType,
                    explicitSetting.allowPush(),
                    explicitSetting.allowEmail(),
                    explicitSetting.allowInApp(),
                    true
            );
        }

        DefaultChannelFlags defaults = NotificationDefaultChannelPolicy.resolve(eventType).orElseThrow();
        return new EffectiveNotificationSetting(
                eventType,
                defaults.push(),
                defaults.email(),
                defaults.inApp(),
                false
        );
    }
}
