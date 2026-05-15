package com.twohands.auth_service.application.auth.register;

public record RegisterUserCommand(
        String email,
        String password,
        String confirmPassword,
        String ipAddress
) {
}
