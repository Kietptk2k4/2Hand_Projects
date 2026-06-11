package com.twohands.social_service.application.integration.handlecommerceproductremoved;

public record HandleCommerceProductRemovedResult(
        String productId,
        long postsUpdated,
        boolean duplicate
) {
}
