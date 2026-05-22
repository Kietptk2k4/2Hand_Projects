package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ReplyToReviewRepository {

    Optional<ReviewForSellerReply> findReviewById(UUID reviewId);

    boolean existsReplyByReviewId(UUID reviewId);

    ReplyToReviewResult insertReply(UUID reviewId, UUID sellerId, String content, Instant createdAt);
}
