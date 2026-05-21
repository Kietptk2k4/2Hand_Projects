package com.twohands.commerce_service.domain.shop;

import java.util.Optional;
import java.util.UUID;

public interface SellerShopRepository {

    Optional<SellerShop> findBySellerId(UUID sellerId);
}
