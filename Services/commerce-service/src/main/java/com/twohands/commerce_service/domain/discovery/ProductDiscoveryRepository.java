package com.twohands.commerce_service.domain.discovery;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ProductDiscoveryRepository {

    long countVisibleProductsByCategories(List<UUID> categoryIds, Instant now);

    List<ProductCardSummary> findVisibleProductsByCategories(
            List<UUID> categoryIds,
            ProductDiscoverySort sort,
            PageQuery pageQuery,
            Instant now
    );

    long countVisibleProductsByKeyword(String likePattern, Instant now);

    List<ProductCardSummary> findVisibleProductsByKeyword(
            String likePattern,
            ProductDiscoverySort sort,
            PageQuery pageQuery,
            Instant now
    );

    long countAllVisibleProducts(Instant now);

    List<ProductCardSummary> findAllVisibleProducts(
            ProductDiscoverySort sort,
            PageQuery pageQuery,
            Instant now
    );
}
