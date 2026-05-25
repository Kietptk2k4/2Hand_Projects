package com.twohands.notification_service.delivery.http.devicetoken.response;

import com.twohands.notification_service.domain.devicetoken.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record RegisterDeviceTokenResponse(
        UUID id,
        DeviceType deviceType,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        Instant lastUsedAt,
        boolean alreadyRegistered
) {
}
