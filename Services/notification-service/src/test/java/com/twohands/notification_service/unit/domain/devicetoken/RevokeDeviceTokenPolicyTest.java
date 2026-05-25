package com.twohands.notification_service.unit.domain.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.RevokeDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RevokeDeviceTokenPolicyTest {

    private static final UUID TOKEN_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant CREATED_AT = Instant.parse("2026-05-20T08:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-05-24T12:00:00Z");

    @Test
    void apply_deactivatesActiveToken() {
        var token = new UserDeviceToken(
                TOKEN_ID,
                USER_ID,
                DeviceType.ANDROID,
                "fcm-token",
                true,
                CREATED_AT,
                CREATED_AT,
                CREATED_AT
        );

        var outcome = RevokeDeviceTokenPolicy.apply(token, UPDATED_AT);

        assertTrue(outcome.changed());
        assertFalse(outcome.token().active());
        assertEquals(UPDATED_AT, outcome.token().updatedAt());
    }

    @Test
    void apply_isIdempotentForInactiveToken() {
        var token = new UserDeviceToken(
                TOKEN_ID,
                USER_ID,
                DeviceType.IOS,
                "fcm-token",
                false,
                CREATED_AT,
                CREATED_AT,
                CREATED_AT
        );

        var outcome = RevokeDeviceTokenPolicy.apply(token, UPDATED_AT);

        assertFalse(outcome.changed());
        assertFalse(outcome.token().active());
        assertEquals(CREATED_AT, outcome.token().updatedAt());
    }
}
