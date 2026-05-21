package com.twohands.notification_service.domain.devicetoken;

import java.time.Instant;
import java.util.UUID;

public record UserDeviceToken(
        UUID id,
        UUID userId,
        DeviceType deviceType,
        String deviceToken,
        boolean active,
        Instant updatedAt,
        Instant lastUsedAt,
        Instant createdAt
) {
}
