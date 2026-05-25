package com.twohands.notification_service.domain.devicetoken;

public record RegisterDeviceTokenDecision(
        DeviceType deviceType,
        String deviceToken,
        boolean alreadyRegistered
) {
}
