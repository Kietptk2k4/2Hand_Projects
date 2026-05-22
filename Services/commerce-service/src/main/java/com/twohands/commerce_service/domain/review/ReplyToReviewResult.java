package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.UUID;

public record ReplyToReviewResult(
        UUID replyId,
        UUID reviewId,
        UUID sellerId,
        String content,
        Instant createdAt
) {
}
