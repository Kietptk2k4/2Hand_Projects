package com.twohands.notification_service.delivery.http.settings.request;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationSettingRequest(
        @NotNull Boolean allowPush,
        @NotNull Boolean allowEmail,
        @NotNull Boolean allowInApp
) {
}
