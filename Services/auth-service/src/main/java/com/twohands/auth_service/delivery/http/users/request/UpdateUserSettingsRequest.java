package com.twohands.auth_service.delivery.http.users.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserSettingsRequest(
        @JsonProperty("appearance_mode")
        @NotBlank(message = "Appearance mode is required")
        String appearanceMode
) {
}
