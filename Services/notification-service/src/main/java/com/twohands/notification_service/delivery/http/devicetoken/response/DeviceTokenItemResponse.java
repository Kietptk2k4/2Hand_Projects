package com.twohands.notification_service.delivery.http.devicetoken.response;

import com.twohands.notification_service.domain.devicetoken.DeviceType;

import java.time.Instant;
import java.util.UUID;

public record DeviceTokenItemResponse(
        UUID id,
        DeviceType deviceType,
        String maskedDeviceToken,
        boolean active,
        Instant lastUsedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
