package com.twohands.social_service.application.post.uploadpostmedia;

import java.util.UUID;

public record UploadPostMediaCommand(
        UUID userId,
        String contentType,
        long fileSizeBytes,
        String mediaKind,
        String clientUploadOrigin
) {
}
