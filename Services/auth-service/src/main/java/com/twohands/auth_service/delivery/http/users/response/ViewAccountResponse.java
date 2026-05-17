package com.twohands.auth_service.delivery.http.users.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

public record ViewAccountResponse(
        UserData user,
        ProfileData profile,
        SettingsData settings
) {
    public record UserData(
            String id,
            String email,
            String status,
            @JsonProperty("email_verified")
            Boolean emailVerified,
            String phone,
            @JsonProperty("last_login_at")
            Instant lastLoginAt
    ) {
    }

    public record ProfileData(
            @JsonProperty("display_name")
            String displayName,
            @JsonProperty("avatar_url")
            String avatarUrl,
            String bio,
            String website,
            @JsonProperty("social_links")
            Map<String, String> socialLinks,
            @JsonProperty("is_private")
            boolean isPrivate
    ) {
    }

    public record SettingsData(
            @JsonProperty("appearance_mode")
            String appearanceMode
    ) {
    }
}
