package com.twohands.social_service.application.search.searchpost;

import java.util.UUID;

public record SearchPostCommand(
        UUID userId,
        String keyword,
        int page,
        int size
) {
}
