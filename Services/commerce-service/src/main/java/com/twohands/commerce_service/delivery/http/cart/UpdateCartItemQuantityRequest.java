package com.twohands.commerce_service.delivery.http.cart;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
        @Min(value = 1, message = "quantity must be greater than 0")
        int quantity
) {
}
