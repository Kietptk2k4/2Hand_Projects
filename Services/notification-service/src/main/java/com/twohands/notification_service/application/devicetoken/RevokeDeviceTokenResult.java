package com.twohands.notification_service.application.devicetoken;

import java.util.UUID;

public record RevokeDeviceTokenResult(
        UUID id,
        boolean active,
        boolean alreadyRevoked
) {
}
