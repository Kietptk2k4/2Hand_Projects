package com.twohands.commerce_service.delivery.http.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.review.ModerateReviewResult;
import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InternalModerateReviewResponse(
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
    public static InternalModerateReviewResponse from(ModerateReviewResult result) {
        return new InternalModerateReviewResponse(
                result.reviewId(),
                result.orderItemId(),
                result.sellerId(),
                result.buyerId(),
                result.rating(),
                result.status(),
                result.previousStatus(),
                result.alreadyModerated(),
                result.sellerRatingAvg(),
                result.sellerRatingCount(),
                result.moderatedAt()
        );
    }
}
