package com.twohands.social_service.application.post.uploadpostmedia;

import java.time.Instant;
import java.util.List;

public record UploadPostMediaResult(
        String uploadUrl,
        String objectKey,
        String mediaUrl,
        String mediaKind,
        Instant expiresAt,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
}
