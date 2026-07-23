package com.twohands.social_service.delivery.http.admin.response;

import java.time.Instant;

public record RecommendationModelArtifactResponse(
        String modelName,
        int version,
        String format,
        String artifactPath,
        boolean isActive,
        Instant trainedAt,
        Object metrics
) {
}
