package com.twohands.social_service.domain.post;

/**
 * Runtime ranking mode visible to admin/internal status.
 *
 * @param mode                  {@code lightgbm} or {@code rule_based}
 * @param modelVersion          active registry version when lightgbm; otherwise null
 * @param modelName             model name when lightgbm; otherwise null
 * @param reason                machine-readable fallback reason when rule_based; otherwise null
 * @param configuredRankingModel configured {@code social.recommendation.ranking.model}
 */
public record RankingModelRuntimeStatus(
        String mode,
        Integer modelVersion,
        String modelName,
        String reason,
        String configuredRankingModel
) {
}
