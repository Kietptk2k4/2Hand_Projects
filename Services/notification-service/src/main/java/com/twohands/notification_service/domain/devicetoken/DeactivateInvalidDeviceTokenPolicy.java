package com.twohands.notification_service.domain.devicetoken;

import java.time.Instant;

public final class DeactivateInvalidDeviceTokenPolicy {

    private DeactivateInvalidDeviceTokenPolicy() {
    }

    public static boolean isStaleInactiveCandidate(UserDeviceToken token, Instant staleBefore) {
        if (token == null || !token.active()) {
            return false;
        }
        if (token.lastUsedAt() != null) {
            return false;
        }
        return token.updatedAt().isBefore(staleBefore);
    }
}
