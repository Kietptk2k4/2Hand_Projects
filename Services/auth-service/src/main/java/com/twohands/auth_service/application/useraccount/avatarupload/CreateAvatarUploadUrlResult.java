package com.twohands.auth_service.application.useraccount.avatarupload;

import java.time.Instant;
import java.util.List;

public record CreateAvatarUploadUrlResult(
        String uploadUrl,
        String objectKey,
        String avatarUrl,
        Instant expiresAt,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
}
