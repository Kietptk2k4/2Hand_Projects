package com.twohands.notification_service.delivery.http.devicetoken.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDeviceTokenRequest(
        @NotBlank String deviceType,
        @NotBlank @Size(max = 512) String deviceToken
) {
}
