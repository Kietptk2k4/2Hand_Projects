package com.twohands.commerce_service.domain.shop;

import java.util.Optional;
import java.util.UUID;

public interface ViewMyShopRepository {

    Optional<ViewMyShopResult> findBySellerId(UUID sellerId);
}
