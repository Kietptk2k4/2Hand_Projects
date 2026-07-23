package com.twohands.commerce_service.domain.admin;

import java.util.Optional;
import java.util.UUID;

public interface ViewShopDetailForModerationRepository {

    Optional<AdminShopDetailEntry> findByShopId(UUID shopId);
}
