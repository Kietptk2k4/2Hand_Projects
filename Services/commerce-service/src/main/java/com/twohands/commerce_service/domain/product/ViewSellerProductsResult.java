package com.twohands.commerce_service.domain.product;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;

public record ViewSellerProductsResult(
        List<SellerProductListItem> items,
        PageMeta pagination,
        SellerProductListSummary summary
) {
}