package com.twohands.auth_service.application.auth.adminlogin;

import java.util.List;
import java.util.UUID;

public record AdminLoginResult(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UUID userId,
        String email,
        String status,
        List<String> roles,
        List<String> permissions
) {
}
