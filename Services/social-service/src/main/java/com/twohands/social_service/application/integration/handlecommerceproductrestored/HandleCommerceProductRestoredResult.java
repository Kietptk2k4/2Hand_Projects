package com.twohands.social_service.application.integration.handlecommerceproductrestored;

public record HandleCommerceProductRestoredResult(
        String productId,
        long postsUpdated,
        boolean duplicate
) {
}
