package com.twohands.commerce_service.application.product.viewproductsbyshop;

import java.util.UUID;

public record ViewProductsByShopCommand(
        UUID shopId,
        Integer page,
        Integer limit,
        String sort
) {
}
