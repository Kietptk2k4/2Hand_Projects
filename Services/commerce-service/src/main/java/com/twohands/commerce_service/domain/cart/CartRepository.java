package com.twohands.commerce_service.domain.cart;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Optional<Cart> findByUserId(UUID userId);

    Cart getOrCreateByUserId(UUID userId);

    Cart save(Cart cart);

    void updateTimestamp(UUID cartId, Instant updatedAt);
}
