package com.twohands.auth_service.delivery.http.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RefreshAccessTokenRequest(
        @JsonProperty("refresh_token")
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
