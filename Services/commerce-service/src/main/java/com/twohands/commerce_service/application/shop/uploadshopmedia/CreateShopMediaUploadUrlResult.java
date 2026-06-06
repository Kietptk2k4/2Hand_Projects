package com.twohands.commerce_service.application.shop.uploadshopmedia;

import java.time.Instant;
import java.util.List;

public record CreateShopMediaUploadUrlResult(
        String uploadUrl,
        String objectKey,
        String mediaUrl,
        String mediaKind,
        Instant expiresAt,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
}
