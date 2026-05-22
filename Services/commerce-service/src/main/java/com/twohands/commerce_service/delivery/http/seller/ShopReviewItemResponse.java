package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewMediaResponse;
import com.twohands.commerce_service.delivery.http.catalog.ProductReviewSellerReplyResponse;
import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ShopReviewItemResponse(
        @JsonProperty("review_id") UUID reviewId,
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("product_name_snapshot") String productNameSnapshot,
        int rating,
        String comment,
        ReviewStatus status,
        @JsonProperty("created_at") Instant createdAt,
        List<ProductReviewMediaResponse> media,
        @JsonProperty("seller_reply") ProductReviewSellerReplyResponse sellerReply
) {
}
