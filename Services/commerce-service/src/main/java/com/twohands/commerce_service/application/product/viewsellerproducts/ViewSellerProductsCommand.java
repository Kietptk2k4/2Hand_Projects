package com.twohands.commerce_service.application.product.viewsellerproducts;

import java.util.UUID;

public record ViewSellerProductsCommand(
        UUID sellerId,
        Integer page,
        Integer limit,
        String status,
        String keyword
) {
}
