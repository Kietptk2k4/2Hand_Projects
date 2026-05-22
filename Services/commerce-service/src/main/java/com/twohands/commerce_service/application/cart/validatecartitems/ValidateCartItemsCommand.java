package com.twohands.commerce_service.application.cart.validatecartitems;

import java.util.List;
import java.util.UUID;

public record ValidateCartItemsCommand(
        UUID userId,
        List<UUID> cartItemIds
) {
}
