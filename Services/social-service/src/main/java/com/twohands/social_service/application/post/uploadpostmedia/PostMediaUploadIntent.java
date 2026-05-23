package com.twohands.social_service.application.post.uploadpostmedia;

import java.time.Instant;

public record PostMediaUploadIntent(
        String uploadUrl,
        String objectKey,
        String mediaUrl,
        String mediaKind,
        Instant expiresAt
) {
}
