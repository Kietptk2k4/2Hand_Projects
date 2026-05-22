package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record ReviewMediaItem(
        UUID id,
        String url,
        ReviewMediaType type
) {
}
