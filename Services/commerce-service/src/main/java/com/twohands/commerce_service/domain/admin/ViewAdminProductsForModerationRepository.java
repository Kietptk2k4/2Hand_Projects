package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.util.List;
import java.util.Optional;

public interface ViewAdminProductsForModerationRepository {

    long count(Optional<ProductStatus> status, Optional<String> searchQuery);

    List<AdminProductListEntry> find(
            Optional<ProductStatus> status,
            Optional<String> searchQuery,
            PageQuery pageQuery
    );
}
