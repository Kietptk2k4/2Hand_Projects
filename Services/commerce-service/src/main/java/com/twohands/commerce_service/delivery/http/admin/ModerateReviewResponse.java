package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ModerateReviewResponse(
        @JsonProperty("review_id") UUID reviewId,
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("buyer_id") UUID buyerId,
        int rating,
        ReviewStatus status,
        @JsonProperty("previous_status") ReviewStatus previousStatus,
        @JsonProperty("already_moderated") boolean alreadyModerated,
        @JsonProperty("seller_rating_avg") BigDecimal sellerRatingAvg,
        @JsonProperty("seller_rating_count") int sellerRatingCount,
        @JsonProperty("moderated_at") Instant moderatedAt
) {
}
