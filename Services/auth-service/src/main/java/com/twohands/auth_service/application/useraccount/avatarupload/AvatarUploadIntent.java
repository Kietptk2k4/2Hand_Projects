package com.twohands.auth_service.application.useraccount.avatarupload;

import java.time.Instant;

public record AvatarUploadIntent(
        String uploadUrl,
        String objectKey,
        String avatarUrl,
        Instant expiresAt
) {
}
