package com.twohands.social_service.delivery.http.admin.response;

import java.time.Instant;

public record RecommendationModelArtifactResponse(
        int version,
        String format,
        String artifactPath,
        boolean isActive,
        Instant trainedAt,
        Object metrics
) {
}
