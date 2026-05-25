package com.twohands.notification_service.delivery.http.devicetoken.response;

import java.util.UUID;

public record RevokeDeviceTokenResponse(
        UUID id,
        boolean active,
        boolean alreadyRevoked
) {
}
