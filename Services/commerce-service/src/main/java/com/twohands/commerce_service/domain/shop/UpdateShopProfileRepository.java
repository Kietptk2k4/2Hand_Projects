package com.twohands.commerce_service.domain.shop;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UpdateShopProfileRepository {

    Optional<UpdateShopProfileSnapshot> findBySellerId(UUID sellerId);

    UpdateShopProfileResult updateProfile(UpdateShopProfileDraft draft, Instant updatedAt);
}
