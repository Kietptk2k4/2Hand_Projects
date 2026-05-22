package com.twohands.commerce_service.application.product.publishproduct;

import java.util.UUID;

public record PublishProductCommand(UUID sellerId, UUID productId) {
}
