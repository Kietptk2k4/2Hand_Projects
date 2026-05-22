package com.twohands.commerce_service.unit.domain.cart;

import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartItemSyncEvaluator;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemSyncEvaluatorTest {

    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();

    @Test
    void shouldMarkInvalidWhenProductMissing() {
        Optional<CartItemStatus> status = CartItemSyncEvaluator.resolveTargetStatus(
                CartItemStatus.ACTIVE,
                null,
                2,
                true
        );

        assertThat(status).contains(CartItemStatus.INVALID_PRODUCT);
    }

    @Test
    void shouldMarkOutOfStockWhenQuantityExceedsStock() {
        Optional<CartItemStatus> status = CartItemSyncEvaluator.resolveTargetStatus(
                CartItemStatus.ACTIVE,
                activeContext(3),
                5,
                true
        );

        assertThat(status).contains(CartItemStatus.OUT_OF_STOCK);
    }

    @Test
    void shouldRestoreActiveWhenStockSufficient() {
        Optional<CartItemStatus> status = CartItemSyncEvaluator.resolveTargetStatus(
                CartItemStatus.OUT_OF_STOCK,
                activeContext(10),
                2,
                false
        );

        assertThat(status).contains(CartItemStatus.ACTIVE);
    }

    @Test
    void shouldNotRestoreRemovedItems() {
        Optional<CartItemStatus> status = CartItemSyncEvaluator.resolveTargetStatus(
                CartItemStatus.REMOVED,
                activeContext(10),
                1,
                true
        );

        assertThat(status).isEmpty();
    }

    @Test
    void shouldNotRestoreInvalidByDefault() {
        Optional<CartItemStatus> status = CartItemSyncEvaluator.resolveTargetStatus(
                CartItemStatus.INVALID_PRODUCT,
                activeContext(10),
                1,
                false
        );

        assertThat(status).isEmpty();
    }

    @Test
    void shouldRestoreInvalidWhenAllowedAndShopActive() {
        Optional<CartItemStatus> status = CartItemSyncEvaluator.resolveTargetStatus(
                CartItemStatus.INVALID_PRODUCT,
                activeContext(10),
                2,
                true
        );

        assertThat(status).contains(CartItemStatus.ACTIVE);
    }

    @Test
    void shouldMarkInvalidWhenShopSuspended() {
        ProductPurchaseContext context = new ProductPurchaseContext(
                productId,
                sellerId,
                shopId,
                "Phone",
                ProductStatus.ACTIVE,
                ShopStatus.SUSPENDED,
                true,
                500,
                10,
                new ActiveProductPrice(new BigDecimal("100000"), null, new BigDecimal("100000")),
                null
        );

        Optional<CartItemStatus> status = CartItemSyncEvaluator.resolveTargetStatus(
                CartItemStatus.ACTIVE,
                context,
                1,
                true
        );

        assertThat(status).contains(CartItemStatus.INVALID_PRODUCT);
    }

    private ProductPurchaseContext activeContext(int stockQuantity) {
        return new ProductPurchaseContext(
                productId,
                sellerId,
                shopId,
                "Phone",
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                true,
                500,
                stockQuantity,
                new ActiveProductPrice(new BigDecimal("100000"), null, new BigDecimal("100000")),
                null
        );
    }
}
