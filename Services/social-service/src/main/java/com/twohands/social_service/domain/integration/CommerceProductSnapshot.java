package com.twohands.social_service.domain.integration;

public record CommerceProductSnapshot(
        String productId,
        String title,
        String imageUrl,
        String categoryName
) {
}
