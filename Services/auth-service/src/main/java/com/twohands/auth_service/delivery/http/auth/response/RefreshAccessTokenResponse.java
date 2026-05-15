package com.twohands.auth_service.delivery.http.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefreshAccessTokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        long expiresIn
) {
}
