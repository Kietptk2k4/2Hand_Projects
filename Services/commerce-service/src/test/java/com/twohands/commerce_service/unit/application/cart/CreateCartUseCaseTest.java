package com.twohands.commerce_service.unit.application.cart;

import com.twohands.commerce_service.application.cart.createcart.CreateCartCommand;
import com.twohands.commerce_service.application.cart.createcart.CreateCartResult;
import com.twohands.commerce_service.application.cart.createcart.CreateCartUseCase;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCartUseCaseTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CreateCartUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();

    @Test
    void shouldCreateNewCartWithEmptyItemsWhenBuyerHasNoCart() {
        Instant now = Instant.now();
        Cart newCart = new Cart(cartId, userId, now, now);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(cartRepository.getOrCreateByUserId(userId)).thenReturn(newCart);
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId)).thenReturn(List.of());

        CreateCartResult result = useCase.execute(new CreateCartCommand(userId));

        assertThat(result.newlyCreated()).isTrue();
        assertThat(result.cartId()).isEqualTo(cartId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.items()).isEmpty();
        assertThat(useCase.successMessage(true)).isEqualTo("Tao gio hang thanh cong.");
        verify(cartRepository).getOrCreateByUserId(userId);
    }

    @Test
    void shouldReturnExistingCartWithoutCreatingWhenCartAlreadyExists() {
        Instant now = Instant.now();
        Cart existing = new Cart(cartId, userId, now, now);
        UUID itemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        CartItem activeItem = new CartItem(
                itemId, cartId, productId, sellerId, 2, CartItemStatus.ACTIVE, now, now
        );
        CartItem removedItem = new CartItem(
                UUID.randomUUID(), cartId, UUID.randomUUID(), sellerId, 1, CartItemStatus.REMOVED, now, now
        );

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(cartItemRepository.findByCartIdExcludingRemoved(cartId))
                .thenReturn(List.of(activeItem));

        CreateCartResult result = useCase.execute(new CreateCartCommand(userId));

        assertThat(result.newlyCreated()).isFalse();
        assertThat(result.cartId()).isEqualTo(cartId);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().cartItemId()).isEqualTo(itemId);
        assertThat(useCase.successMessage(false)).isEqualTo("Lay gio hang thanh cong.");
        verify(cartRepository, never()).getOrCreateByUserId(userId);
        verify(cartItemRepository).findByCartIdExcludingRemoved(cartId);
    }
}
