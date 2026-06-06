package com.twohands.commerce_service.application.shop.uploadshopmedia;

import java.time.Instant;

public record ShopMediaUploadIntent(
        String uploadUrl,
        String objectKey,
        String mediaUrl,
        String mediaKind,
        Instant expiresAt
) {
}
