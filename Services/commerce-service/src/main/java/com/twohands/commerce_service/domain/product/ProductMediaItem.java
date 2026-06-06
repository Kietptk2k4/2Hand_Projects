package com.twohands.commerce_service.domain.product;

public record ProductMediaItem(
        String mediaUrl,
        String mediaType,
        int sortOrder
) {
}
