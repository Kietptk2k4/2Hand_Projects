package com.twohands.notification_service.domain.delivery;

public record DefaultChannelFlags(
        boolean inApp,
        boolean push,
        boolean email
) {

    public boolean hasAnyChannel() {
        return inApp || push || email;
    }
}
