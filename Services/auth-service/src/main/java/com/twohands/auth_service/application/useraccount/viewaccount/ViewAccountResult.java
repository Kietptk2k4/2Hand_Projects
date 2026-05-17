package com.twohands.auth_service.application.useraccount.viewaccount;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ViewAccountResult(
        UserData user,
        ProfileData profile,
        SettingsData settings
) {
    public record UserData(
            UUID id,
            String email,
            String status,
            Boolean emailVerified,
            String phone,
            Instant lastLoginAt
    ) {
    }

    public record ProfileData(
            String displayName,
            String avatarUrl,
            String bio,
            String website,
            Map<String, String> socialLinks,
            boolean isPrivate
    ) {
    }

    public record SettingsData(
            String appearanceMode
    ) {
    }
}
