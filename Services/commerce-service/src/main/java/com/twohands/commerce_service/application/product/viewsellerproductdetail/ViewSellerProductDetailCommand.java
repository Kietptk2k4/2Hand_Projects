package com.twohands.commerce_service.application.product.viewsellerproductdetail;

import java.util.UUID;

public record ViewSellerProductDetailCommand(
        UUID sellerId,
        UUID productId
) {
}
