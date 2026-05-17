package com.twohands.auth_service.application.useraccount.viewpublicuserprofile;

import java.util.Map;
import java.util.UUID;

public record ViewPublicUserProfileResult(
        UUID userId,
        String displayName,
        String avatarUrl,
        String bio,
        String website,
        Map<String, String> socialLinks,
        boolean isPrivate
) {
}
