package com.twohands.auth_service.delivery.http.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdminRefreshAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        long expiresIn,
        AdminLoginResponse.UserInfo user,
        List<String> roles,
        List<String> permissions
) {
}
