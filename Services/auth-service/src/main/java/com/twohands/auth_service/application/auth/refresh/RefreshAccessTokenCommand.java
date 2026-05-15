package com.twohands.auth_service.application.auth.refresh;

public record RefreshAccessTokenCommand(
        String refreshToken,
        String ipAddress
) {
}
