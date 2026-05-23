package com.twohands.notification_service.unit.domain.delivery;

import com.twohands.notification_service.domain.delivery.NotificationCriticalOverridePolicy;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationDefaultChannelPolicyTest {

    @Test
    void resolve_returnsSocialDefaults() {
        var flags = NotificationDefaultChannelPolicy.resolve("POST_LIKED").orElseThrow();

        assertTrue(flags.inApp());
        assertTrue(flags.push());
        assertFalse(flags.email());
    }

    @Test
    void resolve_returnsEmptyForUnknownEventType() {
        assertTrue(NotificationDefaultChannelPolicy.resolve("UNKNOWN_EVENT").isEmpty());
    }

    @Test
    void isKnownEventType_returnsTrueForConfiguredEvent() {
        assertTrue(NotificationDefaultChannelPolicy.isKnownEventType("PAYMENT_SUCCESS"));
        assertFalse(NotificationDefaultChannelPolicy.isKnownEventType("UNKNOWN_EVENT"));
    }
}
