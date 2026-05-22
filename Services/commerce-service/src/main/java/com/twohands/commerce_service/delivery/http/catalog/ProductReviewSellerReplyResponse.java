package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ProductReviewSellerReplyResponse(
        @JsonProperty("reply_id") UUID replyId,
        String content,
        @JsonProperty("created_at") Instant createdAt
) {
}
