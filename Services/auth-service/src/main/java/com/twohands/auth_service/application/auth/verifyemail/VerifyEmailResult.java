package com.twohands.auth_service.application.auth.verifyemail;

public record VerifyEmailResult(
        String userId,
        boolean emailVerified,
        String status,
        String message
) {
}
