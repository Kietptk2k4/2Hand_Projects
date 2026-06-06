package com.twohands.commerce_service.domain.product;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewSellerProductCatalogRepository {

    long countBySellerId(UUID sellerId, Optional<ProductStatus> status, Optional<String> keyword);

    List<SellerProductListItem> findBySellerId(
            UUID sellerId,
            Optional<ProductStatus> status,
            Optional<String> keyword,
            PageQuery pageQuery,
            Instant now
    );

    SellerProductListSummary summarizeBySellerId(UUID sellerId);

    Optional<SellerProductDetail> findDetailBySellerId(UUID sellerId, UUID productId, Instant now);
}
