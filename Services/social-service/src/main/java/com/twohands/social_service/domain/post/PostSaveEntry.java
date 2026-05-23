package com.twohands.social_service.domain.post;

import java.time.Instant;

public record PostSaveEntry(
        String postId,
        Instant savedAt
) {
}
