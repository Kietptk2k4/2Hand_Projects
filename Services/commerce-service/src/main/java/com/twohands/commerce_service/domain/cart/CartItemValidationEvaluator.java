package com.twohands.commerce_service.domain.cart;

import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.util.Optional;

public final class CartItemValidationEvaluator {

    private CartItemValidationEvaluator() {
    }

    public static Optional<CartItemValidationReason> resolveInvalidReason(
            CartItemStatus currentStatus,
            ProductPurchaseContext context,
            int cartQuantity,
            boolean shopOnVacation
    ) {
        if (currentStatus == CartItemStatus.REMOVED) {
            return Optional.of(CartItemValidationReason.CART_ITEM_REMOVED);
        }

        Optional<CartItemStatus> targetStatus = CartItemSyncEvaluator.resolveTargetStatus(
                currentStatus,
                context,
                cartQuantity,
                true
        );

        if (targetStatus.isEmpty()) {
            return Optional.of(mapStatusToReason(currentStatus));
        }

        CartItemStatus resolved = targetStatus.get();
        if (resolved == CartItemStatus.INVALID_PRODUCT) {
            return Optional.of(resolveInvalidProductReason(context));
        }
        if (resolved == CartItemStatus.OUT_OF_STOCK) {
            return Optional.of(CartItemValidationReason.OUT_OF_STOCK);
        }

        if (context == null) {
            return Optional.of(CartItemValidationReason.PRODUCT_NOT_FOUND);
        }
        if (context.activePrice() == null) {
            return Optional.of(CartItemValidationReason.ACTIVE_PRICE_MISSING);
        }
        if (shopOnVacation) {
            return Optional.of(CartItemValidationReason.SHOP_ON_VACATION);
        }

        return Optional.empty();
    }

    public static Optional<CartItemStatus> resolvePersistedStatus(
            CartItemStatus currentStatus,
            ProductPurchaseContext context,
            int cartQuantity
    ) {
        return CartItemSyncEvaluator.resolveTargetStatus(currentStatus, context, cartQuantity, true);
    }

    private static CartItemValidationReason mapStatusToReason(CartItemStatus status) {
        return switch (status) {
            case REMOVED -> CartItemValidationReason.CART_ITEM_REMOVED;
            case OUT_OF_STOCK -> CartItemValidationReason.OUT_OF_STOCK;
            case INVALID_PRODUCT -> CartItemValidationReason.INVALID_PRODUCT;
            case ACTIVE -> CartItemValidationReason.INVALID_PRODUCT;
        };
    }

    private static CartItemValidationReason resolveInvalidProductReason(ProductPurchaseContext context) {
        if (context == null) {
            return CartItemValidationReason.PRODUCT_NOT_FOUND;
        }
        if (context.shopStatus() != ShopStatus.ACTIVE) {
            return CartItemValidationReason.SHOP_NOT_ACTIVE;
        }
        if (!context.categoryActive()) {
            return CartItemValidationReason.CATEGORY_INACTIVE;
        }
        if (context.productStatus() != ProductStatus.ACTIVE && context.productStatus() != ProductStatus.OUT_OF_STOCK) {
            return CartItemValidationReason.PRODUCT_NOT_ACTIVE;
        }
        return CartItemValidationReason.INVALID_PRODUCT;
    }
}
