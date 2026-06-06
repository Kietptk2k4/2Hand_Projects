package com.twohands.commerce_service.application.product.uploadproductmedia;

import java.util.List;

public record CreateProductMediaUploadUrlResult(
        String uploadUrl,
        String objectKey,
        String mediaUrl,
        String mediaKind,
        String expiresAt,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
}