package com.twohands.commerce_service.unit.application.cart;

import com.twohands.commerce_service.application.cart.removecartitem.RemoveCartItemCommand;
import com.twohands.commerce_service.application.cart.removecartitem.RemoveCartItemResult;
import com.twohands.commerce_service.application.cart.removecartitem.RemoveCartItemUseCase;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
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
class RemoveCartItemUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private RemoveCartItemUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @Test
    void shouldMarkActiveCartItemAsRemoved() {
        Cart cart = new Cart(cartId, userId, now, now);
        CartItem active = new CartItem(
                cartItemId, cartId, productId, sellerId, 2, CartItemStatus.ACTIVE, now, now
        );
        CartItem removed = active.withStatus(CartItemStatus.REMOVED, now);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCartId(cartItemId, cartId)).thenReturn(Optional.of(active));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(removed);
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of());

        RemoveCartItemResult result = useCase.execute(new RemoveCartItemCommand(userId, cartItemId));

        assertThat(result.status()).isEqualTo(CartItemStatus.REMOVED);
        assertThat(result.alreadyRemoved()).isFalse();
        assertThat(result.activeItemCount()).isZero();

        ArgumentCaptor<CartItem> itemCaptor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(itemCaptor.capture());
        assertThat(itemCaptor.getValue().status()).isEqualTo(CartItemStatus.REMOVED);
        verify(cartRepository).updateTimestamp(eq(cartId), any(Instant.class));
    }

    @Test
    void shouldReturnIdempotentResultWhenAlreadyRemoved() {
        Cart cart = new Cart(cartId, userId, now, now);
        CartItem removedItem = new CartItem(
                cartItemId, cartId, productId, sellerId, 1, CartItemStatus.REMOVED, now, now
        );

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCartId(cartItemId, cartId)).thenReturn(Optional.of(removedItem));
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of());

        RemoveCartItemResult result = useCase.execute(new RemoveCartItemCommand(userId, cartItemId));

        assertThat(result.alreadyRemoved()).isTrue();
        verify(cartItemRepository, never()).save(any());
        verify(cartRepository, never()).updateTimestamp(any(), any());
    }

    @Test
    void shouldRejectWhenCartItemNotFound() {
        Cart cart = new Cart(cartId, userId, now, now);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByIdAndCartId(cartItemId, cartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RemoveCartItemCommand(userId, cartItemId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenUserHasNoCart() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RemoveCartItemCommand(userId, cartItemId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenCartItemBelongsToAnotherUserCart() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
        when(cartItemRepository.findByIdAndCartId(cartItemId, cartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RemoveCartItemCommand(userId, cartItemId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
    }
}
