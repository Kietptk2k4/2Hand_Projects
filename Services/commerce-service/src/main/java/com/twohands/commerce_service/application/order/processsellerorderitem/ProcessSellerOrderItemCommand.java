package com.twohands.commerce_service.application.order.processsellerorderitem;

import java.util.List;
import java.util.UUID;

public record ProcessSellerOrderItemCommand(
        UUID sellerId,
        List<UUID> orderItemIds
) {
}
