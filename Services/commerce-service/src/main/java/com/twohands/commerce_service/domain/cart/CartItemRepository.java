package com.twohands.commerce_service.domain.cart;

import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository {

    Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

    CartItem save(CartItem cartItem);
}
