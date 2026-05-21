package com.twohands.commerce_service.application.cart.createcart;

import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreateCartUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CreateCartUseCase(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public CreateCartResult execute(CreateCartCommand command) {
        Optional<Cart> existing = cartRepository.findByUserId(command.userId());
        Cart cart = existing.orElseGet(() -> cartRepository.getOrCreateByUserId(command.userId()));
        boolean newlyCreated = existing.isEmpty();
        List<CreateCartItemResult> items = cartItemRepository.findByCartIdExcludingRemoved(cart.id()).stream()
                .map(this::toItemResult)
                .toList();
        return new CreateCartResult(
                cart.id(),
                cart.userId(),
                items,
                cart.createdAt(),
                cart.updatedAt(),
                newlyCreated
        );
    }

    public String successMessage(boolean newlyCreated) {
        return newlyCreated
                ? "Tao gio hang thanh cong."
                : "Lay gio hang thanh cong.";
    }

    private CreateCartItemResult toItemResult(CartItem item) {
        return new CreateCartItemResult(
                item.id(),
                item.productId(),
                item.sellerId(),
                item.quantity(),
                item.status()
        );
    }
}
