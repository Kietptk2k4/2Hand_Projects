package com.twohands.auth_service.application.auth.oauth;

import java.util.UUID;

public record BootstrapOAuthSessionResult(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UUID userId,
        String email,
        String status
) {
}
