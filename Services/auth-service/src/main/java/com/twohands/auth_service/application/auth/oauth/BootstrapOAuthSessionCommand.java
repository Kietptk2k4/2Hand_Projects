package com.twohands.auth_service.application.auth.oauth;

public record BootstrapOAuthSessionCommand(
        String accessToken,
        String refreshToken
) {
}
