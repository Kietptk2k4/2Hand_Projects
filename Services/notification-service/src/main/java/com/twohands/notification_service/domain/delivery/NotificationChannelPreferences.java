package com.twohands.notification_service.domain.delivery;

public record NotificationChannelPreferences(
        boolean inApp,
        boolean push,
        boolean email
) {

    public boolean hasAnyChannel() {
        return inApp || push || email;
    }
}
