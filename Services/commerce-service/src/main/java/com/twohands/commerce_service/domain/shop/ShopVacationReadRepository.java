package com.twohands.commerce_service.domain.shop;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ShopVacationReadRepository {

    Map<UUID, Boolean> findVacationByShopIds(Collection<UUID> shopIds);
}
