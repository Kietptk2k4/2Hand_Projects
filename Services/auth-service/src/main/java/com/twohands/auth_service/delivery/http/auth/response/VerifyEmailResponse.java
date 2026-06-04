package com.twohands.auth_service.delivery.http.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VerifyEmailResponse(
        @JsonProperty("user_id")
        String userId,
        @JsonProperty("email_verified")
        boolean emailVerified,
        String status
) {
}
