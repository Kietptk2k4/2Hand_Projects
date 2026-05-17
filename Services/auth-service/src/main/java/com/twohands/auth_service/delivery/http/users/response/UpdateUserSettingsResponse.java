package com.twohands.auth_service.delivery.http.users.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateUserSettingsResponse(
        @JsonProperty("appearance_mode")
        String appearanceMode
) {
}
