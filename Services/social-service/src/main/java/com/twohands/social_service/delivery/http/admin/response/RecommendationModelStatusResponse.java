package com.twohands.social_service.delivery.http.admin.response;

public record RecommendationModelStatusResponse(
        String mode,
        Integer modelVersion,
        String modelName,
        String reason,
        String configuredRankingModel
) {
}
