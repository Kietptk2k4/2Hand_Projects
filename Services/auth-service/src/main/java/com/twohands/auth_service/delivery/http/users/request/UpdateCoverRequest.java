package com.twohands.auth_service.delivery.http.users.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UpdateCoverRequest(
        @JsonProperty("cover_url")
        @NotBlank(message = "Cover URL is required")
        String coverUrl
) {
}
