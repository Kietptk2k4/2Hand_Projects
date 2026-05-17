package com.twohands.auth_service.delivery.http.users.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateProfileRequest(
        @JsonProperty("display_name")
        @NotBlank(message = "Display name is required")
        @Size(max = 100, message = "Display name max length is 100")
        String displayName,

        @Size(max = 500, message = "Bio max length is 500")
        String bio,

        String website,

        @JsonProperty("social_links")
        Map<String, String> socialLinks
) {
}
