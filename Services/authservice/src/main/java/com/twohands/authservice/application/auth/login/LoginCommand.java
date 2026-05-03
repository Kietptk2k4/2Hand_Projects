package com.twohands.authservice.application.auth.login;

public record LoginCommand(
        String email,
        String password,
        String ipAddress,
        String userAgent,
        String deviceId
) {
}
