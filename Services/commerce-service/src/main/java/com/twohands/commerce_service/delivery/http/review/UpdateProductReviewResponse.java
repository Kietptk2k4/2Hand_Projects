package com.twohands.commerce_service.delivery.http.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateProductReviewResponse(
        @JsonProperty("review_id") UUID reviewId,
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("buyer_id") UUID buyerId,
        int rating,
        String comment,
        ReviewStatus status,
        @JsonProperty("rating_changed") boolean ratingChanged,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("seller_rating_avg") BigDecimal sellerRatingAvg,
        @JsonProperty("seller_rating_count") int sellerRatingCount
) {
}
