package com.twohands.notification_service.unit.domain.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.RegisterDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterDeviceTokenPolicyTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-05-24T12:00:00Z");

    @Test
    void parseDeviceType_acceptsCaseInsensitiveValues() {
        assertEquals(DeviceType.IOS, RegisterDeviceTokenPolicy.parseDeviceType("ios"));
        assertEquals(DeviceType.ANDROID, RegisterDeviceTokenPolicy.parseDeviceType(" ANDROID "));
    }

    @Test
    void parseDeviceType_rejectsInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> RegisterDeviceTokenPolicy.parseDeviceType("DESKTOP"));
    }

    @Test
    void normalizeDeviceToken_trimsAndValidatesLength() {
        assertEquals("abc123", RegisterDeviceTokenPolicy.normalizeDeviceToken(" abc123 "));
    }

    @Test
    void normalizeDeviceToken_rejectsBlankToken() {
        assertThrows(IllegalArgumentException.class, () -> RegisterDeviceTokenPolicy.normalizeDeviceToken("  "));
    }

    @Test
    void resolve_marksAlreadyRegisteredWhenSameUserTypeAndActive() {
        var existing = Optional.of(new UserDeviceToken(
                UUID.randomUUID(),
                USER_ID,
                DeviceType.ANDROID,
                "token-1",
                true,
                NOW,
                NOW,
                NOW
        ));

        var decision = RegisterDeviceTokenPolicy.resolve(
                DeviceType.ANDROID,
                "token-1",
                existing,
                USER_ID
        );

        assertTrue(decision.alreadyRegistered());
    }

    @Test
    void resolve_doesNotMarkAlreadyRegisteredForInactiveToken() {
        var existing = Optional.of(new UserDeviceToken(
                UUID.randomUUID(),
                USER_ID,
                DeviceType.ANDROID,
                "token-1",
                false,
                NOW,
                NOW,
                NOW
        ));

        var decision = RegisterDeviceTokenPolicy.resolve(
                DeviceType.ANDROID,
                "token-1",
                existing,
                USER_ID
        );

        assertFalse(decision.alreadyRegistered());
    }

    @Test
    void maskDeviceToken_hidesFullValue() {
        assertEquals("****7890", RegisterDeviceTokenPolicy.maskDeviceToken("abcdef7890"));
    }
}
