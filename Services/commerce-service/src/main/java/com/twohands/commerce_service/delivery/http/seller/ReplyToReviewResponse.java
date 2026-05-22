package com.twohands.commerce_service.delivery.http.seller;

import java.time.Instant;
import java.util.UUID;

public record ReplyToReviewResponse(
        UUID replyId,
        UUID reviewId,
        UUID sellerId,
        String content,
        Instant createdAt
) {
}
