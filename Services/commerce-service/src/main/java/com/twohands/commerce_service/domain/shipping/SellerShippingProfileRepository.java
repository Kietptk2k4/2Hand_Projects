package com.twohands.commerce_service.domain.shipping;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface SellerShippingProfileRepository {

    Map<UUID, SellerShippingProfile> findByShopIds(Collection<UUID> shopIds);
}
