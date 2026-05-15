package com.twohands.auth_service.delivery.http.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("expires_in")
        long expiresIn,
        UserInfo user
) {
    public record UserInfo(
            String id,
            String email,
            String status
    ) {
    }
}
