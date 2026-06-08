package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.common.pagination.PageQuery;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.util.List;
import java.util.Optional;

public interface ViewAdminShopsForModerationRepository {

    long count(Optional<ShopStatus> status, Optional<String> searchQuery);

    List<AdminShopListEntry> find(
            Optional<ShopStatus> status,
            Optional<String> searchQuery,
            AdminShopListSort sort,
            PageQuery pageQuery
    );
}
