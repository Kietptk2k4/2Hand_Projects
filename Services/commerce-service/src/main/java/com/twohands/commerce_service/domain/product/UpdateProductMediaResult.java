package com.twohands.commerce_service.domain.product;

import java.util.List;
import java.util.UUID;

public record UpdateProductMediaResult(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        String thumbnailUrl,
        List<String> mediaUrls,
        boolean hasMedia
) {
}
