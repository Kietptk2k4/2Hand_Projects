package com.twohands.commerce_service.domain.home;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HomeRecommendationReadRepository {

    List<CommerceInteraction> findCompletedInteractions(UUID userId, Instant since, Instant asOf);

    List<CommerceInteraction> findCartInteractions(UUID userId, Instant since, Instant asOf);

    List<SocialTagSignal> findSocialSignals(UUID userId);

    List<HomeProductSnapshot> findNewestProducts(UUID excludedSellerId, Instant asOf, int limit);

    List<HomeProductSnapshot> findPopularProducts90d(UUID excludedSellerId, Instant since, Instant asOf, int limit);

    List<HomeProductSnapshot> findTopRatedProducts(UUID excludedSellerId, Instant asOf, int limit);

    List<HomeProductSnapshot> findProductsByCategories(
            Collection<UUID> categoryIds,
            UUID excludedSellerId,
            Instant asOf,
            int limit
    );

    List<HomeProductSnapshot> findProductsByBrands(
            Collection<UUID> brandIds,
            UUID excludedSellerId,
            Instant asOf,
            int limit
    );

    List<HomeProductSnapshot> findProductsByShops(
            Collection<UUID> shopIds,
            UUID excludedSellerId,
            Instant asOf,
            int limit
    );

    List<EntityNeighbor> findCategoryNeighbors(Collection<UUID> categoryIds, int limitPerSeed);

    List<EntityNeighbor> findBrandNeighbors(Collection<UUID> brandIds, int limitPerSeed);

    List<AssociationRule> findAssociationRules(String tagType, Collection<String> tags, double minConfidence);

    record CommerceInteraction(
            UUID categoryId,
            UUID brandId,
            UUID shopId,
            BigDecimal unitPrice,
            Instant occurredAt,
            double baseWeight
    ) {
    }

    record SocialTagSignal(
            String tagType,
            String tag,
            double score,
            Instant computedAt,
            Instant asOf
    ) {
    }

    record EntityNeighbor(UUID seedId, UUID neighborId, double score) {
    }

    record AssociationRule(String tag, UUID categoryId, double confidence) {
    }
}
