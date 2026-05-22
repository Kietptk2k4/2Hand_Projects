package com.twohands.commerce_service.application.order.vieworderlist;

import java.util.UUID;

public record ViewOrderListCommand(
        UUID buyerId,
        Integer page,
        Integer limit,
        String status
) {
}
