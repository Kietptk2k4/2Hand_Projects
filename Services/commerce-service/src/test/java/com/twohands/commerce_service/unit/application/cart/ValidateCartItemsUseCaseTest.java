package com.twohands.commerce_service.unit.application.cart;

import com.twohands.commerce_service.application.cart.validatecartitems.ValidateCartItemsCommand;
import com.twohands.commerce_service.application.cart.validatecartitems.ValidateCartItemsUseCase;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.cart.ValidateCartItemsResult;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.domain.shop.ShopVacationReadRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class ValidateCartItemsUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductPurchaseReadRepository productPurchaseReadRepository;

    @Mock
    private ShopVacationReadRepository shopVacationReadRepository;

    private ValidateCartItemsUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ValidateCartItemsUseCase(
                cartRepository,
                cartItemRepository,
                productPurchaseReadRepository,
                shopVacationReadRepository,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReturnEmptyResultWhenCartMissing() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        ValidateCartItemsResult result = useCase.execute(new ValidateCartItemsCommand(userId, null));

        assertThat(result.canCheckout()).isFalse();
        assertThat(result.validItems()).isEmpty();
        assertThat(result.invalidItems()).isEmpty();
    }

    @Test
    void shouldValidateAllNonRemovedItems() {
        CartItem cartItem = cartItem(CartItemStatus.ACTIVE, 2);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of(cartItem));
        when(productPurchaseReadRepository.findByProductIds(any())).thenReturn(Map.of(productId, activeContext(10)));
        when(shopVacationReadRepository.findVacationByShopIds(any())).thenReturn(Map.of(shopId, false));

        ValidateCartItemsResult result = useCase.execute(new ValidateCartItemsCommand(userId, null));

        assertThat(result.canCheckout()).isTrue();
        assertThat(result.validItems()).hasSize(1);
        assertThat(result.invalidItems()).isEmpty();
    }

    @Test
    void shouldPersistOutOfStockStatus() {
        CartItem cartItem = cartItem(CartItemStatus.ACTIVE, 5);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of(cartItem));
        when(productPurchaseReadRepository.findByProductIds(any())).thenReturn(Map.of(productId, activeContext(2)));
        when(shopVacationReadRepository.findVacationByShopIds(any())).thenReturn(Map.of(shopId, false));
        when(cartItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ValidateCartItemsResult result = useCase.execute(new ValidateCartItemsCommand(userId, null));

        assertThat(result.canCheckout()).isFalse();
        assertThat(result.invalidItems()).hasSize(1);
        assertThat(result.invalidItems().getFirst().reason()).isEqualTo("OUT_OF_STOCK");
        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(CartItemStatus.OUT_OF_STOCK);
        verify(cartRepository).updateTimestamp(eq(cartId), eq(now));
    }

    @Test
    void shouldRejectUnknownCartItemId() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
        when(cartItemRepository.findByCartIdAndIds(eq(cartId), any())).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new ValidateCartItemsCommand(userId, List.of(cartItemId))))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    void shouldBlockCheckoutWhenShopOnVacation() {
        CartItem cartItem = cartItem(CartItemStatus.ACTIVE, 1);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of(cartItem));
        when(productPurchaseReadRepository.findByProductIds(any())).thenReturn(Map.of(productId, activeContext(10)));
        when(shopVacationReadRepository.findVacationByShopIds(any())).thenReturn(Map.of(shopId, true));

        ValidateCartItemsResult result = useCase.execute(new ValidateCartItemsCommand(userId, null));

        assertThat(result.canCheckout()).isFalse();
        assertThat(result.invalidItems().getFirst().reason()).isEqualTo("SHOP_ON_VACATION");
        verify(cartItemRepository, never()).save(any());
    }

    private CartItem cartItem(CartItemStatus status, int quantity) {
        return new CartItem(cartItemId, cartId, productId, sellerId, quantity, status, now, now);
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
