package com.twohands.commerce_service.delivery.http.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateProductReviewResponse(
        @JsonProperty("review_id") UUID reviewId,
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("buyer_id") UUID buyerId,
        @JsonProperty("rating") int rating,
        @JsonProperty("comment") String comment,
        @JsonProperty("status") ReviewStatus status,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("seller_rating_avg") BigDecimal sellerRatingAvg,
        @JsonProperty("seller_rating_count") int sellerRatingCount
) {
}
