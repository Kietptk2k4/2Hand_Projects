package com.twohands.commerce_service.application.order.viewordersforsupport;

import com.twohands.commerce_service.domain.order.OrderSupportListEntry;

import java.util.List;

public record ViewOrdersForSupportResult(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<OrderSupportListEntry> orders
) {
}
