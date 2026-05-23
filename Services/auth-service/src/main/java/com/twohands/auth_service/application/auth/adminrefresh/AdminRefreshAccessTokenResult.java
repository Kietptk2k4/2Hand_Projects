package com.twohands.auth_service.application.auth.adminrefresh;

import java.util.List;
import java.util.UUID;

public record AdminRefreshAccessTokenResult(
        String accessToken,
        long expiresIn,
        UUID userId,
        String email,
        String status,
        List<String> roles,
        List<String> permissions
) {
}
