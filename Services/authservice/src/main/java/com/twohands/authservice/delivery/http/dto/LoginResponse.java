package com.twohands.authservice.delivery.http.dto;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserSummary user
) {
    public record UserSummary(
            UUID id,
            String email,
            String status,
            boolean emailVerified,
            List<String> roles
    ) {
    }
}
