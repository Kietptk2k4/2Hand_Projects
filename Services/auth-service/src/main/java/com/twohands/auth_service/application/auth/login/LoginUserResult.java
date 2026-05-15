package com.twohands.auth_service.application.auth.login;

import java.util.UUID;

public record LoginUserResult(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UUID userId,
        String email,
        String status
) {
}
