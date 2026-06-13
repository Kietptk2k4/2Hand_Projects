package com.twohands.commerce_service.delivery.http.shop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewRatingSummaryResponse;

import java.util.List;
import java.util.UUID;

public record ViewPublicShopReviewsResponse(
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        @JsonProperty("shop_avatar_url") String shopAvatarUrl,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("rating_summary") ProductReviewRatingSummaryResponse ratingSummary,
        List<PublicShopReviewItemResponse> reviews,
        PageMetaResponse pagination
) {
}
