package com.twohands.commerce_service.domain.cart;

import java.util.List;

public record ValidateCartItemsResult(
        List<ValidCartItem> validItems,
        List<InvalidCartItem> invalidItems,
        boolean canCheckout
) {
    public static ValidateCartItemsResult empty() {
        return new ValidateCartItemsResult(List.of(), List.of(), false);
    }
}
