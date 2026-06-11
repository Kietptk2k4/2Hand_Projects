package com.twohands.commerce_service.application.order.viewsellerorderdetail;

import java.util.UUID;

public record ViewSellerOrderDetailCommand(
        UUID sellerId,
        UUID orderId
) {
}
