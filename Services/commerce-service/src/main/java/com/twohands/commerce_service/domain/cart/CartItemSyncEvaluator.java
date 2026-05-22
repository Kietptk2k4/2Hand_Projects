package com.twohands.commerce_service.domain.cart;

import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.util.Optional;

public final class CartItemSyncEvaluator {

    private CartItemSyncEvaluator() {
    }

    /**
     * @param allowInvalidRestore when true, re-evaluates {@code INVALID_PRODUCT} items (e.g. shop restored).
     */
    public static Optional<CartItemStatus> resolveTargetStatus(
            CartItemStatus currentStatus,
            ProductPurchaseContext context,
            int cartQuantity,
            boolean allowInvalidRestore
    ) {
        if (currentStatus == CartItemStatus.REMOVED) {
            return Optional.empty();
        }
        if (currentStatus == CartItemStatus.INVALID_PRODUCT && !allowInvalidRestore) {
            return Optional.empty();
        }

        if (context == null) {
            return Optional.of(CartItemStatus.INVALID_PRODUCT);
        }

        if (isInvalidProductOrShop(context)) {
            return Optional.of(CartItemStatus.INVALID_PRODUCT);
        }

        if (context.productStatus() == ProductStatus.OUT_OF_STOCK || cartQuantity > context.stockQuantity()) {
            return Optional.of(CartItemStatus.OUT_OF_STOCK);
        }

        return Optional.of(CartItemStatus.ACTIVE);
    }

    private static boolean isInvalidProductOrShop(ProductPurchaseContext context) {
        if (context.shopStatus() != ShopStatus.ACTIVE) {
            return true;
        }
        if (!context.categoryActive()) {
            return true;
        }
        return switch (context.productStatus()) {
            case ACTIVE, OUT_OF_STOCK -> false;
            default -> true;
        };
    }
}
