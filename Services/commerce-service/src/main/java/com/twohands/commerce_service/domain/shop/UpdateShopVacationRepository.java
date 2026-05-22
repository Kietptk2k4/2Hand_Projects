package com.twohands.commerce_service.domain.shop;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UpdateShopVacationRepository {

    Optional<UpdateShopVacationSnapshot> findBySellerId(UUID sellerId);

    UpdateShopVacationResult updateVacationSettings(UpdateShopVacationDraft draft, Instant updatedAt);
}
