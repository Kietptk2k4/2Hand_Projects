package com.twohands.commerce_service.application.cart.removecartitem;

import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RemoveCartItemUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public RemoveCartItemUseCase(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public RemoveCartItemResult execute(RemoveCartItemCommand command) {
        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByIdAndCartId(command.cartItemId(), cart.id())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.isRemoved()) {
            return buildResult(cart.id(), cartItem, cartItem.updatedAt(), true);
        }

        Instant now = Instant.now();
        CartItem removed = cartItemRepository.save(cartItem.withStatus(CartItemStatus.REMOVED, now));
        cartRepository.updateTimestamp(cart.id(), now);

        return buildResult(cart.id(), removed, now, false);
    }

    public String successMessage(boolean alreadyRemoved) {
        return alreadyRemoved
                ? "San pham da duoc xoa khoi gio hang truoc do."
                : "Xoa san pham khoi gio hang thanh cong.";
    }

    private RemoveCartItemResult buildResult(
            UUID cartId,
            CartItem cartItem,
            Instant removedAt,
            boolean alreadyRemoved
    ) {
        int activeItemCount = cartItemRepository.findByCartIdExcludingRemoved(cartId).size();
        return new RemoveCartItemResult(
                cartId,
                cartItem.id(),
                cartItem.productId(),
                CartItemStatus.REMOVED,
                removedAt,
                activeItemCount,
                alreadyRemoved
        );
    }
}
