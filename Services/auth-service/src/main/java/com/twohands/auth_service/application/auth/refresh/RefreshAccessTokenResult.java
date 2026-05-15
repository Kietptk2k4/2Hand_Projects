package com.twohands.auth_service.application.auth.refresh;

public record RefreshAccessTokenResult(
        String accessToken,
        long expiresIn
) {
}
