package com.twohands.commerce_service.domain.cart;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    Optional<CartItem> findByIdAndCartId(UUID cartItemId, UUID cartId);

    List<CartItem> findByCartIdExcludingRemoved(UUID cartId);

    List<CartItem> findByCartIdAndIds(UUID cartId, Collection<UUID> cartItemIds);

    CartItem save(CartItem cartItem);

    int markInvalidByProductId(UUID productId, Instant updatedAt);

    int markInvalidBySellerId(UUID sellerId, Instant updatedAt);
}
