package com.twohands.notification_service.delivery.http.devicetoken.response;

import java.util.List;

public record ViewUserDeviceTokensResponse(
        List<DeviceTokenItemResponse> items
) {
}
