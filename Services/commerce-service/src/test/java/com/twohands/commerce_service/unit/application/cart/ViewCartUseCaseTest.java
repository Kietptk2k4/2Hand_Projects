package com.twohands.commerce_service.unit.application.cart;

import com.twohands.commerce_service.application.cart.viewcart.ViewCartCommand;
import com.twohands.commerce_service.application.cart.viewcart.ViewCartUseCase;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.cart.ViewCartResult;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.ShopVacationReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewCartUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductPurchaseReadRepository productPurchaseReadRepository;

    @Mock
    private ShopVacationReadRepository shopVacationReadRepository;

    private ViewCartUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ViewCartUseCase(
                cartRepository,
                cartItemRepository,
                productPurchaseReadRepository,
                shopVacationReadRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldGetOrCreateEmptyCart() {
        Cart cart = new Cart(cartId, userId, now, now);
        when(cartRepository.getOrCreateByUserId(userId)).thenReturn(cart);
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of());

        ViewCartResult result = useCase.execute(new ViewCartCommand(userId));

        assertThat(result.cartId()).isEqualTo(cartId);
        assertThat(result.items()).isEmpty();
        assertThat(result.summary().canCheckout()).isFalse();
        assertThat(result.summary().subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(cartRepository).getOrCreateByUserId(userId);
    }

    @Test
    void shouldReturnEnrichedCartWithSubtotal() {
        Cart cart = new Cart(cartId, userId, now, now);
        CartItem cartItem = new CartItem(cartItemId, cartId, productId, sellerId, 2, CartItemStatus.ACTIVE, now, now);
        when(cartRepository.getOrCreateByUserId(userId)).thenReturn(cart);
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of(cartItem));
        when(productPurchaseReadRepository.findByProductIds(any())).thenReturn(Map.of(productId, activeContext(10)));
        when(shopVacationReadRepository.findVacationByShopIds(any())).thenReturn(Map.of(shopId, false));

        ViewCartResult result = useCase.execute(new ViewCartCommand(userId));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().productName()).isEqualTo("Phone");
        assertThat(result.items().getFirst().effectivePrice()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(result.items().getFirst().inStock()).isTrue();
        assertThat(result.items().getFirst().unavailableReason()).isNull();
        assertThat(result.summary().subtotal()).isEqualByComparingTo(new BigDecimal("200000"));
        assertThat(result.summary().canCheckout()).isTrue();
        assertThat(result.summary().activeItemCount()).isEqualTo(1);
        assertThat(result.summary().invalidItemCount()).isZero();
    }

    @Test
    void shouldMarkOutOfStockAndBlockCheckout() {
        Cart cart = new Cart(cartId, userId, now, now);
        CartItem cartItem = new CartItem(cartItemId, cartId, productId, sellerId, 5, CartItemStatus.ACTIVE, now, now);
        when(cartRepository.getOrCreateByUserId(userId)).thenReturn(cart);
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of(cartItem));
        when(productPurchaseReadRepository.findByProductIds(any())).thenReturn(Map.of(productId, activeContext(2)));
        when(shopVacationReadRepository.findVacationByShopIds(any())).thenReturn(Map.of(shopId, false));
        when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ViewCartResult result = useCase.execute(new ViewCartCommand(userId));

        assertThat(result.items().getFirst().status()).isEqualTo(CartItemStatus.OUT_OF_STOCK);
        assertThat(result.items().getFirst().unavailableReason()).isEqualTo("OUT_OF_STOCK");
        assertThat(result.summary().canCheckout()).isFalse();
        assertThat(result.summary().invalidItemCount()).isEqualTo(1);
        assertThat(result.summary().warnings()).isNotEmpty();
        verify(cartRepository).updateTimestamp(eq(cartId), eq(now));
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
                "http://localhost:9000/2hands-commerce-product/p1.jpg"
        );
    }
}
