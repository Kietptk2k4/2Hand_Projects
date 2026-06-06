package com.twohands.commerce_service.application.shop.uploadshopmedia;

import java.util.UUID;

public record CreateShopMediaUploadUrlCommand(
        UUID sellerId,
        String contentType,
        long fileSizeBytes,
        String mediaKind
) {
}
