package com.twohands.auth_service.delivery.http.users.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record TogglePrivacyRequest(
        @JsonProperty("is_private")
        @NotNull(message = "is_private is required")
        Boolean isPrivate
) {
}
