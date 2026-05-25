package com.twohands.notification_service.application.devicetoken;

import com.twohands.notification_service.domain.devicetoken.UserDeviceTokenView;

import java.util.List;
import java.util.UUID;

public record ViewUserDeviceTokensResult(
        UUID userId,
        List<UserDeviceTokenView> items
) {
}
