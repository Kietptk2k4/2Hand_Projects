package com.twohands.commerce_service.unit.domain.cart;

import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartItemValidationEvaluator;
import com.twohands.commerce_service.domain.cart.CartItemValidationReason;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemValidationEvaluatorTest {

    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();

    @Test
    void shouldRejectRemovedItem() {
        Optional<CartItemValidationReason> reason = CartItemValidationEvaluator.resolveInvalidReason(
                CartItemStatus.REMOVED,
                activeContext(10),
                1,
                false
        );

        assertThat(reason).contains(CartItemValidationReason.CART_ITEM_REMOVED);
    }

    @Test
    void shouldRejectOutOfStock() {
        Optional<CartItemValidationReason> reason = CartItemValidationEvaluator.resolveInvalidReason(
                CartItemStatus.ACTIVE,
                activeContext(2),
                5,
                false
        );

        assertThat(reason).contains(CartItemValidationReason.OUT_OF_STOCK);
    }

    @Test
    void shouldRejectMissingActivePrice() {
        ProductPurchaseContext context = new ProductPurchaseContext(
                productId,
                sellerId,
                shopId,
                "Phone",
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                true,
                500,
                10,
                null,
                null
        );

        Optional<CartItemValidationReason> reason = CartItemValidationEvaluator.resolveInvalidReason(
                CartItemStatus.ACTIVE,
                context,
                1,
                false
        );

        assertThat(reason).contains(CartItemValidationReason.ACTIVE_PRICE_MISSING);
    }

    @Test
    void shouldRejectShopOnVacation() {
        Optional<CartItemValidationReason> reason = CartItemValidationEvaluator.resolveInvalidReason(
                CartItemStatus.ACTIVE,
                activeContext(10),
                1,
                true
        );

        assertThat(reason).contains(CartItemValidationReason.SHOP_ON_VACATION);
    }

    @Test
    void shouldAcceptActiveItem() {
        Optional<CartItemValidationReason> reason = CartItemValidationEvaluator.resolveInvalidReason(
                CartItemStatus.ACTIVE,
                activeContext(10),
                2,
                false
        );

        assertThat(reason).isEmpty();
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
