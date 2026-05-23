package com.twohands.auth_service.application.auth.adminrefresh;

public record AdminRefreshAccessTokenCommand(
        String refreshToken,
        String ipAddress
) {
}
