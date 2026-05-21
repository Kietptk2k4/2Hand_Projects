package com.twohands.commerce_service.domain.shop;

import java.time.Instant;
import java.util.UUID;

public interface CreateShopRepository {

    boolean existsBySellerId(UUID sellerId);

    CreateShopResult create(CreateShopDraft draft, Instant occurredAt);
}
