package com.twohands.commerce_service.delivery.http.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.review.MyProductReviewSnapshot;
import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.time.Instant;
import java.util.UUID;

public record ViewMyProductReviewResponse(
        @JsonProperty("has_review") boolean hasReview,
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("review_id") UUID reviewId,
        @JsonProperty("order_item_id") UUID orderItemId,
        Integer rating,
        String comment,
        ReviewStatus status,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("can_edit") boolean canEdit
) {
    public static ViewMyProductReviewResponse from(MyProductReviewSnapshot snapshot) {
        return new ViewMyProductReviewResponse(
                snapshot.hasReview(),
                snapshot.productId(),
                snapshot.reviewId(),
                snapshot.orderItemId(),
                snapshot.rating(),
                snapshot.comment(),
                snapshot.status(),
                snapshot.createdAt(),
                snapshot.updatedAt(),
                snapshot.canEdit()
        );
    }
}
