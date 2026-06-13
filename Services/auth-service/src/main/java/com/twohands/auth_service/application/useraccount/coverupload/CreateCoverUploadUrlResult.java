package com.twohands.auth_service.application.useraccount.coverupload;

import java.time.Instant;
import java.util.List;

public record CreateCoverUploadUrlResult(
        String uploadUrl,
        String objectKey,
        String coverUrl,
        Instant expiresAt,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
}
