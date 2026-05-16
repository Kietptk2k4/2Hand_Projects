package com.twohands.auth_service.application.auth.oauth;

public record OAuthLoginCommand(
        OAuthProfile profile,
        String ipAddress,
        String userAgent,
        String deviceId
) {
}
