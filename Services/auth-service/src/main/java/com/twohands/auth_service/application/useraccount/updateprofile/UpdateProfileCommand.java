package com.twohands.auth_service.application.useraccount.updateprofile;

import java.util.Map;
import java.util.UUID;

public record UpdateProfileCommand(
        UUID userId,
        String displayName,
        String bio,
        String website,
        Map<String, String> socialLinks
) {
}
