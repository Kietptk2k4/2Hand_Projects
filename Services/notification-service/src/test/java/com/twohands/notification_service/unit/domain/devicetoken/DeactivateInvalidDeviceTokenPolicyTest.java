package com.twohands.notification_service.unit.domain.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeactivateInvalidDeviceTokenPolicy;
import com.twohands.notification_service.domain.devicetoken.DeviceType;
import com.twohands.notification_service.domain.devicetoken.UserDeviceToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeactivateInvalidDeviceTokenPolicyTest {

    private static final Instant STALE_BEFORE = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant OLD_UPDATED = Instant.parse("2025-06-01T00:00:00Z");
    private static final Instant RECENT_UPDATED = Instant.parse("2026-05-01T00:00:00Z");

    @Test
    void isStaleInactiveCandidate_trueForActiveNeverUsedTokenUpdatedBeforeCutoff() {
        var token = new UserDeviceToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                DeviceType.IOS,
                "token-a",
                true,
                OLD_UPDATED,
                null,
                OLD_UPDATED
        );

        assertTrue(DeactivateInvalidDeviceTokenPolicy.isStaleInactiveCandidate(token, STALE_BEFORE));
    }

    @Test
    void isStaleInactiveCandidate_falseWhenTokenInactive() {
        var token = new UserDeviceToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                DeviceType.IOS,
                "token-a",
                false,
                OLD_UPDATED,
                null,
                OLD_UPDATED
        );

        assertFalse(DeactivateInvalidDeviceTokenPolicy.isStaleInactiveCandidate(token, STALE_BEFORE));
    }

    @Test
    void isStaleInactiveCandidate_falseWhenLastUsedAtPresent() {
        var token = new UserDeviceToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                DeviceType.IOS,
                "token-a",
                true,
                OLD_UPDATED,
                OLD_UPDATED,
                OLD_UPDATED
        );

        assertFalse(DeactivateInvalidDeviceTokenPolicy.isStaleInactiveCandidate(token, STALE_BEFORE));
    }

    @Test
    void isStaleInactiveCandidate_falseWhenUpdatedRecently() {
        var token = new UserDeviceToken(
                UUID.randomUUID(),
                UUID.randomUUID(),
                DeviceType.IOS,
                "token-a",
                true,
                RECENT_UPDATED,
                null,
                RECENT_UPDATED
        );

        assertFalse(DeactivateInvalidDeviceTokenPolicy.isStaleInactiveCandidate(token, STALE_BEFORE));
    }
}
