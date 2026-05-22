package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewRatingSummaryResponse;

import java.util.List;
import java.util.UUID;

public record ViewShopReviewsResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("rating_summary") ProductReviewRatingSummaryResponse ratingSummary,
        List<ShopReviewItemResponse> reviews,
        PageMetaResponse pagination
) {
}
