package com.twohands.notification_service.application.devicetoken;

import com.twohands.notification_service.domain.devicetoken.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record RegisterDeviceTokenResult(
        UUID id,
        UUID userId,
        DeviceType deviceType,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant lastUsedAt,
        boolean alreadyRegistered
) {
}
