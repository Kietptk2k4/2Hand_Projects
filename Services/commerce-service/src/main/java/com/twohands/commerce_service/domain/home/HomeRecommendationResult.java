package com.twohands.commerce_service.domain.home;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record HomeRecommendationResult(
        String requestId,
        RankingMode rankingMode,
        String modelName,
        Integer modelVersion,
        List<Item> items
) {
    public record Item(
            UUID id,
            String title,
            BigDecimal price,
            BigDecimal salePrice,
            BigDecimal effectivePrice,
            String thumbnail,
            Shop shop,
            Rating rating
    ) {
    }

    public record Shop(UUID id, String name) {
    }

    public record Rating(BigDecimal avg, int count) {
    }
}
