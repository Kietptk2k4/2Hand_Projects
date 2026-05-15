package com.twohands.auth_service.delivery.http.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegisterResponse(
        @JsonProperty("user_id")
        String userId,
        String email,
        String status
) {
}
