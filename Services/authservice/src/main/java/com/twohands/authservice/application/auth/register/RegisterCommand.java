package com.twohands.authservice.application.auth.register;

public record RegisterCommand(
        String email,
        String password,
        String confirmPassword
) {
}
