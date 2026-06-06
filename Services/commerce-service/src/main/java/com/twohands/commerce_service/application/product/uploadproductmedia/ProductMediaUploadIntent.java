package com.twohands.commerce_service.application.product.uploadproductmedia;

import java.time.Instant;

public record ProductMediaUploadIntent(
        String uploadUrl,
        String objectKey,
        String mediaUrl,
        String mediaKind,
        Instant expiresAt
) {
}