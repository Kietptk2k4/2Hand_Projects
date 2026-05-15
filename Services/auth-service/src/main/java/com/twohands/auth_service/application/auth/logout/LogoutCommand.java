package com.twohands.auth_service.application.auth.logout;

public record LogoutCommand(
        String refreshToken,
        String ipAddress
) {
}
