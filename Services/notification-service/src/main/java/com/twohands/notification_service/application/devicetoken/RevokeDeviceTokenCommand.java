package com.twohands.notification_service.application.devicetoken;

import java.util.UUID;

public record RevokeDeviceTokenCommand(
        UUID userId,
        String deviceToken
) {
}
