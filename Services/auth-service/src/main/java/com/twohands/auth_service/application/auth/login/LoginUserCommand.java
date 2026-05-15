package com.twohands.auth_service.application.auth.login;

public record LoginUserCommand(
        String email,
        String password,
        String ipAddress,
        String userAgent,
        String deviceId
) {
}
