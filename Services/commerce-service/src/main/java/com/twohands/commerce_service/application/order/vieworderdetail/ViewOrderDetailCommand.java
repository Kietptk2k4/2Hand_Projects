package com.twohands.commerce_service.application.order.vieworderdetail;

import java.util.UUID;

public record ViewOrderDetailCommand(
        UUID buyerId,
        UUID orderId
) {
}
