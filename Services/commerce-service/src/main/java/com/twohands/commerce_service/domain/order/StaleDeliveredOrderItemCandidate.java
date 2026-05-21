package com.twohands.commerce_service.domain.order;

import java.util.UUID;

public record StaleDeliveredOrderItemCandidate(UUID orderItemId, UUID orderId) {
}
