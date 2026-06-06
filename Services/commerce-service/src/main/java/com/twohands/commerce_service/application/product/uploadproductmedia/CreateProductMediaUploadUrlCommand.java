package com.twohands.commerce_service.application.product.uploadproductmedia;

import java.util.UUID;

public record CreateProductMediaUploadUrlCommand(
        UUID sellerId,
        UUID productId,
        String contentType,
        long fileSizeBytes,
        String mediaKind
) {
}
