package com.twohands.commerce_service.domain.discovery;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ViewProductsByShopRepository {

    Optional<ViewProductsByShopResult> findVisibleProductsByShopId(
            UUID shopId,
            ProductDiscoverySort sort,
            PageQuery pageQuery,
            Instant now
    );
}
