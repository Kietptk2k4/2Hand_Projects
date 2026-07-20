package com.twohands.social_service.domain.post;

import java.time.Instant;
import java.util.List;

public record PostCandidate(
        String postId,
        String authorId,
        Instant createdAt,
        List<String> hashtags,
        List<ProductTag> productTags,
        long likeCount,
        long commentCount
) {
}
