package com.twohands.social_service.application.integration.handlecommerceproductremoved;

import java.util.UUID;

public record HandleCommerceProductRemovedCommand(
        UUID eventId,
        String productId
) {
}
