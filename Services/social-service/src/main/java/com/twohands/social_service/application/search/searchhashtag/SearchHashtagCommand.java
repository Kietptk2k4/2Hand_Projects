package com.twohands.social_service.application.search.searchhashtag;

import java.util.UUID;

public record SearchHashtagCommand(
        UUID userId,
        String hashtag,
        int page,
        int size
) {
}
