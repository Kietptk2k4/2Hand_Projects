package com.twohands.commerce_service.domain.integration;

import java.util.UUID;

public record UserPublicProfileSummary(
        UUID userId,
        String displayName,
        String avatarUrl
) {
}
