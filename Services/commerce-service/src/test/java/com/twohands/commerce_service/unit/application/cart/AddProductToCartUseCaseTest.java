package com.twohands.commerce_service.unit.application.cart;

import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartCommand;
import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartResult;
import com.twohands.commerce_service.application.cart.addproducttocart.AddProductToCartUseCase;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddProductToCartUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductPurchaseReadRepository productPurchaseReadRepository;

    @InjectMocks
    private AddProductToCartUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();

    @Test
    void shouldAddNewActiveCartItemWhenProductIsPurchasable() {
        stubCart();
        when(productPurchaseReadRepository.findByProductId(productId)).thenReturn(Optional.of(purchasableContext(5)));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            return new CartItem(
                    UUID.randomUUID(),
                    item.cartId(),
                    item.productId(),
                    item.sellerId(),
                    item.quantity(),
                    item.status(),
                    item.createdAt(),
                    item.updatedAt()
            );
        });

        AddProductToCartResult result = useCase.execute(new AddProductToCartCommand(userId, productId, 2));

        assertThat(result.status()).isEqualTo(CartItemStatus.ACTIVE);
        assertThat(result.quantity()).isEqualTo(2);
        assertThat(result.product().inStock()).isTrue();

        ArgumentCaptor<CartItem> itemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().status()).isEqualTo(CartItemStatus.ACTIVE);
        assertThat(itemCaptor.getValue().sellerId()).isEqualTo(sellerId);
        verify(cartRepository).updateTimestamp(eq(cartId), any(Instant.class));
    }

    @Test
    void shouldUpsertExistingItemInsteadOfCreatingDuplicate() {
        stubCart();
        when(productPurchaseReadRepository.findByProductId(productId)).thenReturn(Optional.of(purchasableContext(10)));

        UUID cartItemId = UUID.randomUUID();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        CartItem existing = new CartItem(
                cartItemId,
                cartId,
                productId,
                sellerId,
                1,
                CartItemStatus.REMOVED,
                createdAt,
                createdAt
        );
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.of(existing));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddProductToCartResult result = useCase.execute(new AddProductToCartCommand(userId, productId, 4));

        assertThat(result.quantity()).isEqualTo(4);
        assertThat(result.status()).isEqualTo(CartItemStatus.ACTIVE);

        ArgumentCaptor<CartItem> itemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().id()).isEqualTo(cartItemId);
        assertThat(itemCaptor.getValue().status()).isEqualTo(CartItemStatus.ACTIVE);
    }

    @Test
    void shouldMarkOutOfStockWhenQuantityExceedsAvailableStock() {
        stubCart();
        when(productPurchaseReadRepository.findByProductId(productId)).thenReturn(Optional.of(purchasableContext(2)));
        when(cartItemRepository.findByCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            return new CartItem(UUID.randomUUID(), item.cartId(), item.productId(), item.sellerId(),
                    item.quantity(), item.status(), item.createdAt(), item.updatedAt());
        });

        AddProductToCartResult result = useCase.execute(new AddProductToCartCommand(userId, productId, 5));

        assertThat(result.status()).isEqualTo(CartItemStatus.OUT_OF_STOCK);
        assertThat(result.product().inStock()).isFalse();
    }

    @Test
    void shouldRejectWhenProductNotFound() {
        stubCart();
        when(productPurchaseReadRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new AddProductToCartCommand(userId, productId, 1)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenProductNotActive() {
        stubCart();
        ProductPurchaseContext context = new ProductPurchaseContext(
                productId, sellerId, shopId, "Phone", ProductStatus.PAUSED, ShopStatus.ACTIVE,
                true, 500, 5, activePrice(), null
        );
        when(productPurchaseReadRepository.findByProductId(productId)).thenReturn(Optional.of(context));

        assertThatThrownBy(() -> useCase.execute(new AddProductToCartCommand(userId, productId, 1)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.NOT_PURCHASABLE);
    }

    @Test
    void shouldRejectWhenStockIsZero() {
        stubCart();
        when(productPurchaseReadRepository.findByProductId(productId)).thenReturn(Optional.of(purchasableContext(0)));

        assertThatThrownBy(() -> useCase.execute(new AddProductToCartCommand(userId, productId, 1)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.OUT_OF_STOCK);
    }

    @Test
    void shouldRejectWhenQuantityIsInvalid() {
        assertThatThrownBy(() -> useCase.execute(new AddProductToCartCommand(userId, productId, 0)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);

        verify(cartRepository, never()).findByUserId(any());
    }

    @Test
    void shouldRejectWhenActivePriceMissing() {
        stubCart();
        ProductPurchaseContext context = new ProductPurchaseContext(
                productId, sellerId, shopId, "Phone", ProductStatus.ACTIVE, ShopStatus.ACTIVE,
                true, 500, 5, null, null
        );
        when(productPurchaseReadRepository.findByProductId(productId)).thenReturn(Optional.of(context));

        assertThatThrownBy(() -> useCase.execute(new AddProductToCartCommand(userId, productId, 1)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.ACTIVE_PRICE_MISSING);
    }

    private void stubCart() {
        Instant now = Instant.now();
        when(cartRepository.findByUserId(userId))
                .thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
    }

    private ProductPurchaseContext purchasableContext(int stockQuantity) {
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
                activePrice(),
                "https://cdn.example.com/product.jpg"
        );
    }

    private ActiveProductPrice activePrice() {
        return new ActiveProductPrice(
                new BigDecimal("1000000"),
                new BigDecimal("900000"),
                new BigDecimal("900000")
        );
    }
}
