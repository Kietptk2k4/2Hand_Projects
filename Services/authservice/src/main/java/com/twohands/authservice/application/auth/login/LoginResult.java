package com.twohands.authservice.application.auth.login;

import java.util.List;
import java.util.UUID;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String email,
        String status,
        boolean emailVerified,
        List<String> roles
) {
}
