package com.twohands.commerce_service.delivery.http.shop;

import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductCardResponse;

import java.util.List;

public record ViewProductsByShopResponse(
        PublicShopSummaryResponse shop,
        List<ProductCardResponse> items,
        PageMetaResponse pagination
) {
}
