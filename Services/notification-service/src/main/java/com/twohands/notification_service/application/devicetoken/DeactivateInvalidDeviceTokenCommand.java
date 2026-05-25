package com.twohands.notification_service.application.devicetoken;

public record DeactivateInvalidDeviceTokenCommand(
        String deviceToken
) {
}
