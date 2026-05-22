package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;

public record ViewSellerOrdersResult(
        List<SellerOrderListEntry> items,
        PageMeta pagination
) {
}
