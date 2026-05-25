package com.twohands.notification_service.unit.domain.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import com.twohands.notification_service.domain.devicetoken.ViewUserDeviceTokensPolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ViewUserDeviceTokensPolicyTest {

    @Test
    void toView_masksDeviceTokenAndMapsFields() {
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-05-20T08:00:00Z");
        Instant updatedAt = Instant.parse("2026-05-24T12:00:00Z");

        var token = new UserDeviceToken(
                id,
                UUID.randomUUID(),
                DeviceType.ANDROID,
                "abcdef7890",
                true,
                updatedAt,
                updatedAt,
                createdAt
        );

        var view = ViewUserDeviceTokensPolicy.toView(token);

        assertEquals(id, view.id());
        assertEquals(DeviceType.ANDROID, view.deviceType());
        assertEquals("****7890", view.maskedDeviceToken());
        assertFalse(view.maskedDeviceToken().contains("abcdef"));
        assertEquals(updatedAt, view.updatedAt());
        assertEquals(createdAt, view.createdAt());
    }
}
