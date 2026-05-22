package com.twohands.commerce_service.application.review.replytoreview;

import java.util.UUID;

public record ReplyToReviewCommand(UUID sellerId, UUID reviewId, String content) {
}
