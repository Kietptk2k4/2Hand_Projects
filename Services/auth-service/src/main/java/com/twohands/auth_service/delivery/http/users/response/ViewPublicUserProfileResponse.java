package com.twohands.auth_service.delivery.http.users.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record ViewPublicUserProfileResponse(
        @JsonProperty("user_id")
        String userId,
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
