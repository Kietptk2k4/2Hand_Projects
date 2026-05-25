package com.twohands.notification_service.domain.devicetoken;

public record RevokeDeviceTokenOutcome(
        UserDeviceToken token,
        boolean changed
) {
}
