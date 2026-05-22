package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.UUID;

public record ProductReviewSellerReply(
        UUID replyId,
        String content,
        Instant createdAt
) {
}
