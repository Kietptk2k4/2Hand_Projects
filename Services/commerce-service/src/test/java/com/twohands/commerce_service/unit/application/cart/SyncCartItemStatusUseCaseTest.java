package com.twohands.commerce_service.unit.application.cart;

import com.twohands.commerce_service.application.cart.synccartitemstatus.SyncCartItemStatusUseCase;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartItemSyncCandidate;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusRepository;
import com.twohands.commerce_service.domain.cart.SyncCartItemStatusResult;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncCartItemStatusUseCaseTest {

    @Mock
    private SyncCartItemStatusRepository syncCartItemStatusRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductPurchaseReadRepository productPurchaseReadRepository;

    private SyncCartItemStatusUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T12:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new SyncCartItemStatusUseCase(
                syncCartItemStatusRepository,
                cartRepository,
                cartItemRepository,
                productPurchaseReadRepository,
                Clock.fixed(now, ZoneOffset.UTC),
                50
        );
    }

    @Test
    void shouldUpdateOutOfStockCartItemForUser() {
        Cart cart = new Cart(cartId, userId, now, now);
        CartItemSyncCandidate candidate = new CartItemSyncCandidate(
                cartItemId, cartId, productId, sellerId, 5, CartItemStatus.ACTIVE
        );
        CartItem existing = new CartItem(cartItemId, cartId, productId, sellerId, 5, CartItemStatus.ACTIVE, now, now);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(syncCartItemStatusRepository.findCandidatesByCartId(cartId)).thenReturn(List.of(candidate));
        when(productPurchaseReadRepository.findByProductIds(any())).thenReturn(Map.of(productId, activeContext(2)));
        when(cartItemRepository.findByIdAndCartId(cartItemId, cartId)).thenReturn(Optional.of(existing));

        SyncCartItemStatusResult result = useCase.syncForUser(userId);

        assertThat(result.updated()).isEqualTo(1);
        ArgumentCaptor<CartItem> saved = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(saved.capture());
        assertThat(saved.getValue().status()).isEqualTo(CartItemStatus.OUT_OF_STOCK);
    }

    @Test
    void shouldSkipRemovedItemsInBatch() {
        CartItemSyncCandidate removed = new CartItemSyncCandidate(
                cartItemId, cartId, productId, sellerId, 1, CartItemStatus.REMOVED
        );
        when(syncCartItemStatusRepository.findCandidateBatch(50)).thenReturn(List.of(removed));

        SyncCartItemStatusResult result = useCase.syncBatch();

        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.updated()).isZero();
        verify(cartItemRepository, never()).save(any());
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
