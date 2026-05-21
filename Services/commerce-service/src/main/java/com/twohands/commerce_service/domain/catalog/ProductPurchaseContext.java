package com.twohands.commerce_service.domain.catalog;

import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.util.UUID;

public record ProductPurchaseContext(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String productTitle,
        ProductStatus productStatus,
        ShopStatus shopStatus,
        boolean categoryActive,
        int stockQuantity,
        ActiveProductPrice activePrice,
        String primaryImageUrl
) {
}
