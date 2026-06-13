package com.twohands.commerce_service.delivery.http.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.review.ReviewContextSnapshot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ViewReviewContextResponse(
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("product_id") UUID productId,
        String status,
        @JsonProperty("product_name_snapshot") String productNameSnapshot,
        @JsonProperty("image_snapshot") String imageSnapshot,
        @JsonProperty("shop_name_snapshot") String shopNameSnapshot,
        @JsonProperty("final_price") BigDecimal finalPrice,
        @JsonProperty("completed_at") Instant completedAt,
        @JsonProperty("has_review") boolean hasReview,
        @JsonProperty("review_id") UUID reviewId
) {
    public static ViewReviewContextResponse from(ReviewContextSnapshot snapshot) {
        return new ViewReviewContextResponse(
                snapshot.orderItemId(),
                snapshot.orderId(),
                snapshot.productId(),
                snapshot.status(),
                snapshot.productNameSnapshot(),
                snapshot.imageSnapshot(),
                snapshot.shopNameSnapshot(),
                snapshot.finalPrice(),
                snapshot.completedAt(),
                snapshot.hasReview(),
                snapshot.reviewId()
        );
    }
}
