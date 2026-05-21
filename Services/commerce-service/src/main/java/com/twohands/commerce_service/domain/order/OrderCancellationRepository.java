package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.UUID;

public interface OrderCancellationRepository {

    BuyerOrderCancellationResult cancelByBuyer(UUID orderId, UUID buyerId, String reason, Instant now);
}
