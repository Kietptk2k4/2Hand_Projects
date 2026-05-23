package com.twohands.auth_service.application.auth.resendemailverification;

public record ResendEmailVerificationCommand(
        String email,
        String ipAddress
) {
}
