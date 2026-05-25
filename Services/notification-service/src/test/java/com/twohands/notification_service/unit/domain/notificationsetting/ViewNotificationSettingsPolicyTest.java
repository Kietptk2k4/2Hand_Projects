package com.twohands.notification_service.unit.domain.notificationsetting;

import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import com.twohands.notification_service.domain.notificationsetting.EffectiveNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import com.twohands.notification_service.domain.notificationsetting.ViewNotificationSettingsPolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewNotificationSettingsPolicyTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-05-24T12:00:00Z");

    @Test
    void resolve_returnsDefaultsWhenNoExplicitSettingsExist() {
        var settings = ViewNotificationSettingsPolicy.resolve(Map.of());

        assertEquals(NotificationDefaultChannelPolicy.supportedEventTypes().size(), settings.size());
        assertTrue(settings.stream().noneMatch(EffectiveNotificationSetting::explicitSetting));

        EffectiveNotificationSetting postLiked = findByEventType(settings, "POST_LIKED");
        assertTrue(postLiked.allowPush());
        assertFalse(postLiked.allowEmail());
        assertTrue(postLiked.allowInApp());
    }

    @Test
    void resolve_usesExplicitSettingWhenRowExists() {
        var explicit = new UserNotificationSetting(
                USER_ID,
                "POST_LIKED",
                false,
                false,
                true,
                NOW,
                NOW
        );

        var settings = ViewNotificationSettingsPolicy.resolve(Map.of("POST_LIKED", explicit));

        EffectiveNotificationSetting postLiked = findByEventType(settings, "POST_LIKED");
        assertFalse(postLiked.allowPush());
        assertFalse(postLiked.allowEmail());
        assertTrue(postLiked.allowInApp());
        assertTrue(postLiked.explicitSetting());
    }

    @Test
    void resolve_returnsSettingsSortedByEventType() {
        var settings = ViewNotificationSettingsPolicy.resolve(Map.of());

        for (int i = 1; i < settings.size(); i++) {
            assertTrue(settings.get(i - 1).eventType().compareTo(settings.get(i).eventType()) <= 0);
        }
    }

    private EffectiveNotificationSetting findByEventType(
            java.util.List<EffectiveNotificationSetting> settings,
            String eventType
    ) {
        return settings.stream()
                .filter(setting -> setting.eventType().equals(eventType))
                .findFirst()
                .orElseThrow();
    }
}
