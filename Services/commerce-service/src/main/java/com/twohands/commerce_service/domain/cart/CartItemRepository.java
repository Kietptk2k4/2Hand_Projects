package com.twohands.commerce_service.domain.cart;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    List<CartItem> findByCartIdAndIds(UUID cartId, Collection<UUID> cartItemIds);

    CartItem save(CartItem cartItem);

    int markInvalidByProductId(UUID productId, Instant updatedAt);
}
