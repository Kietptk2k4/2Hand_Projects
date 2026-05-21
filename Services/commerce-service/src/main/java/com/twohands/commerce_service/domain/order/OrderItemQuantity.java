package com.twohands.commerce_service.domain.order;

import java.util.UUID;

public record OrderItemQuantity(UUID orderItemId, UUID productId, int quantity) {
}
