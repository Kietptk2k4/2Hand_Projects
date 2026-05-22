package com.twohands.commerce_service.application.cart.validatecartitems;

import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartItemValidationEvaluator;
import com.twohands.commerce_service.domain.cart.CartItemValidationReason;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.cart.InvalidCartItem;
import com.twohands.commerce_service.domain.cart.ValidCartItem;
import com.twohands.commerce_service.domain.cart.ValidateCartItemsResult;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.shop.ShopVacationReadRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidateCartItemsUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductPurchaseReadRepository productPurchaseReadRepository;
    private final ShopVacationReadRepository shopVacationReadRepository;
    private final Clock clock;

    public ValidateCartItemsUseCase(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductPurchaseReadRepository productPurchaseReadRepository,
            ShopVacationReadRepository shopVacationReadRepository,
            Clock clock
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productPurchaseReadRepository = productPurchaseReadRepository;
        this.shopVacationReadRepository = shopVacationReadRepository;
        this.clock = clock;
    }

    @Transactional
    public ValidateCartItemsResult execute(ValidateCartItemsCommand command) {
        Optional<Cart> cart = cartRepository.findByUserId(command.userId());
        if (cart.isEmpty()) {
            return ValidateCartItemsResult.empty();
        }

        List<CartItem> itemsToValidate = resolveItemsToValidate(cart.get().id(), command.cartItemIds());
        if (itemsToValidate.isEmpty()) {
            return ValidateCartItemsResult.empty();
        }

        Instant now = clock.instant();
        Set<UUID> productIds = itemsToValidate.stream().map(CartItem::productId).collect(Collectors.toSet());
        Map<UUID, ProductPurchaseContext> contexts = productPurchaseReadRepository.findByProductIds(productIds);

        Set<UUID> shopIds = contexts.values().stream().map(ProductPurchaseContext::shopId).collect(Collectors.toSet());
        Map<UUID, Boolean> vacationByShopId = shopVacationReadRepository.findVacationByShopIds(shopIds);

        List<ValidCartItem> validItems = new ArrayList<>();
        List<InvalidCartItem> invalidItems = new ArrayList<>();

        for (CartItem item : itemsToValidate) {
            ProductPurchaseContext context = contexts.get(item.productId());
            boolean shopOnVacation = context != null && vacationByShopId.getOrDefault(context.shopId(), false);

            CartItemStatus currentStatus = persistStatusIfNeeded(item, context, now);
            Optional<CartItemValidationReason> invalidReason = CartItemValidationEvaluator.resolveInvalidReason(
                    currentStatus,
                    context,
                    item.quantity(),
                    shopOnVacation
            );

            if (invalidReason.isPresent()) {
                invalidItems.add(new InvalidCartItem(
                        item.id(),
                        invalidReason.get().code(),
                        currentStatus
                ));
            } else {
                validItems.add(new ValidCartItem(item.id(), currentStatus));
            }
        }

        boolean canCheckout = !validItems.isEmpty() && invalidItems.isEmpty();
        return new ValidateCartItemsResult(validItems, invalidItems, canCheckout);
    }

    public String successMessage() {
        return "Kiem tra gio hang thanh cong.";
    }

    private List<CartItem> resolveItemsToValidate(UUID cartId, List<UUID> cartItemIds) {
        if (CollectionUtils.isEmpty(cartItemIds)) {
            return cartItemRepository.findByCartIdExcludingRemoved(cartId);
        }
        List<UUID> distinctIds = cartItemIds.stream().distinct().toList();
        List<CartItem> loaded = cartItemRepository.findByCartIdAndIds(cartId, distinctIds);
        if (loaded.size() != distinctIds.size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "One or more cart items were not found");
        }
        return loaded;
    }

    private CartItemStatus persistStatusIfNeeded(CartItem item, ProductPurchaseContext context, Instant now) {
        Optional<CartItemStatus> targetStatus = CartItemValidationEvaluator.resolvePersistedStatus(
                item.status(),
                context,
                item.quantity()
        );
        if (targetStatus.isEmpty() || targetStatus.get() == item.status()) {
            return item.status();
        }
        CartItem updated = cartItemRepository.save(item.withStatus(targetStatus.get(), now));
        cartRepository.updateTimestamp(item.cartId(), now);
        return updated.status();
    }
}
