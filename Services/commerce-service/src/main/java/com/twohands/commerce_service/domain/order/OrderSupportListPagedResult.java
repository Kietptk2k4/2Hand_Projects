package com.twohands.commerce_service.domain.order;

import java.util.List;

public record OrderSupportListPagedResult(
        List<OrderSupportListEntry> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
