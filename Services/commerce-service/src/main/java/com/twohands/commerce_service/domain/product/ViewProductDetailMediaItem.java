package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record ViewProductDetailMediaItem(
        UUID mediaId,
        String mediaUrl,
        String mediaType,
        int sortOrder
) {
}
