package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.common.pagination.PageQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewOrderListRepository {

    long countByBuyerId(UUID buyerId, Optional<OrderStatus> status);

    List<OrderListEntry> findByBuyerId(UUID buyerId, Optional<OrderStatus> status, PageQuery pageQuery);
}
