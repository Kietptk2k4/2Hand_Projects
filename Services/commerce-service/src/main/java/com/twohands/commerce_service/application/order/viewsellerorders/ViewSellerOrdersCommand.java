package com.twohands.commerce_service.application.order.viewsellerorders;

import java.util.UUID;

public record ViewSellerOrdersCommand(
        UUID sellerId,
        Integer page,
        Integer limit,
        String status,
        String shipmentStatus
) {
}
