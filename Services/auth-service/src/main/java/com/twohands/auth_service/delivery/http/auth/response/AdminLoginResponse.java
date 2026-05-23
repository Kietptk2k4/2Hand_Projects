package com.twohands.auth_service.delivery.http.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdminLoginResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("expires_in")
        long expiresIn,
        UserInfo user,
        List<String> roles,
        List<String> permissions
) {
    public record UserInfo(
            String id,
            String email,
            String status
    ) {
    }
}
