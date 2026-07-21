package com.twohands.social_service.domain.integration;

public record CommerceProductSnapshot(
        String productId,
        String title,
        String imageUrl,
        String categoryName,
        String categoryId,
        String shopId
) {
    public CommerceProductSnapshot(String productId, String title, String imageUrl, String categoryName) {
        this(productId, title, imageUrl, categoryName, null, null);
    }
}
