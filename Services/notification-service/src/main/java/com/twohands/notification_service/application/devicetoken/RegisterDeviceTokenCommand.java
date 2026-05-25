package com.twohands.notification_service.application.devicetoken;

import java.util.UUID;

public record RegisterDeviceTokenCommand(
        UUID userId,
        String deviceType,
        String deviceToken
) {
}
