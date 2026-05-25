package com.twohands.notification_service.domain.devicetoken;

import java.time.Instant;

public final class RevokeDeviceTokenPolicy {

    private RevokeDeviceTokenPolicy() {
    }

    public static RevokeDeviceTokenOutcome apply(UserDeviceToken token, Instant updatedAt) {
        if (!token.active()) {
            return new RevokeDeviceTokenOutcome(token, false);
        }

        UserDeviceToken revoked = new UserDeviceToken(
                token.id(),
                token.userId(),
                token.deviceType(),
                token.deviceToken(),
                false,
                updatedAt,
                token.lastUsedAt(),
                token.createdAt()
        );

        return new RevokeDeviceTokenOutcome(revoked, true);
    }
}
