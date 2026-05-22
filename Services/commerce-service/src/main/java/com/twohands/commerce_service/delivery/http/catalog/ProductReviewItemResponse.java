package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductReviewItemResponse(
        @JsonProperty("review_id") UUID reviewId,
        int rating,
        String comment,
        @JsonProperty("created_at") Instant createdAt,
        List<ProductReviewMediaResponse> media,
        @JsonProperty("seller_reply") ProductReviewSellerReplyResponse sellerReply
) {
}
