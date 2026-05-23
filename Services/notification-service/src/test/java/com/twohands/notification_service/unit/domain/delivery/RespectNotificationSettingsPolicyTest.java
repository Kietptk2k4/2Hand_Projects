package com.twohands.notification_service.unit.domain.delivery;

import com.twohands.notification_service.domain.delivery.DefaultChannelFlags;
import com.twohands.notification_service.domain.delivery.RespectNotificationSettingsPolicy;
import com.twohands.notification_service.domain.notificationsetting.UserNotificationSetting;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RespectNotificationSettingsPolicyTest {

    @Test
    void resolve_usesDefaultsWhenSettingMissing() {
        var defaults = new DefaultChannelFlags(true, true, false);

        var preferences = RespectNotificationSettingsPolicy.resolve(Optional.empty(), defaults);

        assertTrue(preferences.inApp());
        assertTrue(preferences.push());
        assertFalse(preferences.email());
    }

    @Test
    void resolve_appliesExplicitUserPreferences() {
        UUID userId = UUID.randomUUID();
        var defaults = new DefaultChannelFlags(true, true, true);
        var setting = new UserNotificationSetting(
                userId,
                "POST_LIKED",
                false,
                false,
                false,
                Instant.now(),
                Instant.now()
        );

        var preferences = RespectNotificationSettingsPolicy.resolve(Optional.of(setting), defaults);

        assertFalse(preferences.inApp());
        assertFalse(preferences.push());
        assertFalse(preferences.email());
        assertFalse(preferences.hasAnyChannel());
    }

    @Test
    void resolve_appliesPartialUserOverrides() {
        UUID userId = UUID.randomUUID();
        var defaults = new DefaultChannelFlags(true, true, false);
        var setting = new UserNotificationSetting(
                userId,
                "POST_LIKED",
                true,
                false,
                false,
                Instant.now(),
                Instant.now()
        );

        var preferences = RespectNotificationSettingsPolicy.resolve(Optional.of(setting), defaults);

        assertFalse(preferences.inApp());
        assertTrue(preferences.push());
        assertFalse(preferences.email());
        assertTrue(preferences.hasAnyChannel());
    }
}
