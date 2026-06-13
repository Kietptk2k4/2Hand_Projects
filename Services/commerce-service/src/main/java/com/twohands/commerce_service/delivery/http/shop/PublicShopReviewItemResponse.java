package com.twohands.commerce_service.delivery.http.shop;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewMediaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewSellerReplyResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicShopReviewItemResponse(
        @JsonProperty("review_id") UUID reviewId,
        @JsonProperty("buyer_id") UUID buyerId,
        @JsonProperty("buyer_display_name") String buyerDisplayName,
        @JsonProperty("buyer_avatar_url") String buyerAvatarUrl,
        @JsonProperty("product_name_snapshot") String productNameSnapshot,
        int rating,
        String comment,
        @JsonProperty("created_at") Instant createdAt,
        List<ProductReviewMediaResponse> media,
        @JsonProperty("seller_reply") ProductReviewSellerReplyResponse sellerReply
) {
}
