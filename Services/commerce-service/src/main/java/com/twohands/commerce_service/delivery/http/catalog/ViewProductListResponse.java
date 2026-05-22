package com.twohands.commerce_service.delivery.http.catalog;

import java.util.List;

public record ViewProductListResponse(
        List<ProductCardResponse> items,
        PageMetaResponse pagination
) {
}
