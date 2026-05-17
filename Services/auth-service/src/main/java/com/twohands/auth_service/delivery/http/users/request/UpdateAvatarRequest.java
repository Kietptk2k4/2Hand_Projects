package com.twohands.auth_service.delivery.http.users.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UpdateAvatarRequest(
        @JsonProperty("avatar_url")
        @NotBlank(message = "Avatar URL is required")
        String avatarUrl
) {
}
