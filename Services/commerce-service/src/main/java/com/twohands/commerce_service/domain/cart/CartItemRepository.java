package com.twohands.commerce_service.domain.cart;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    CartItem save(CartItem cartItem);

    int markInvalidByProductId(UUID productId, Instant updatedAt);
}
