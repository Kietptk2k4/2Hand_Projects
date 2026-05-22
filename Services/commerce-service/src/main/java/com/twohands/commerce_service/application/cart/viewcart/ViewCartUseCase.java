package com.twohands.commerce_service.application.cart.viewcart;

import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartItemValidationEvaluator;
import com.twohands.commerce_service.domain.cart.CartItemValidationReason;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.cart.ViewCartItem;
import com.twohands.commerce_service.domain.cart.ViewCartResult;
import com.twohands.commerce_service.domain.cart.ViewCartSummary;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.shop.ShopVacationReadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ViewCartUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductPurchaseReadRepository productPurchaseReadRepository;
    private final ShopVacationReadRepository shopVacationReadRepository;
    private final Clock clock;

    public ViewCartUseCase(
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
    public ViewCartResult execute(ViewCartCommand command) {
        Cart cart = cartRepository.getOrCreateByUserId(command.userId());
        List<CartItem> cartItems = cartItemRepository.findByCartIdExcludingRemoved(cart.id());
        if (cartItems.isEmpty()) {
            return new ViewCartResult(
                    cart.id(),
                    List.of(),
                    ViewCartSummary.empty(),
                    cart.createdAt(),
                    cart.updatedAt()
            );
        }

        Instant now = clock.instant();
        Set<UUID> productIds = cartItems.stream().map(CartItem::productId).collect(Collectors.toSet());
        Map<UUID, ProductPurchaseContext> contexts = productPurchaseReadRepository.findByProductIds(productIds);
        Set<UUID> shopIds = contexts.values().stream().map(ProductPurchaseContext::shopId).collect(Collectors.toSet());
        Map<UUID, Boolean> vacationByShopId = shopVacationReadRepository.findVacationByShopIds(shopIds);

        List<ViewCartItem> items = new ArrayList<>();
        int activeItemCount = 0;
        int invalidItemCount = 0;
        int checkoutEligibleCount = 0;
        BigDecimal subtotal = BigDecimal.ZERO;
        Set<CartItemValidationReason> warningReasons = new LinkedHashSet<>();

        for (CartItem item : cartItems) {
            ProductPurchaseContext context = contexts.get(item.productId());
            boolean shopOnVacation = context != null && vacationByShopId.getOrDefault(context.shopId(), false);
            CartItemStatus currentStatus = persistStatusIfNeeded(item, context, now);
            Optional<CartItemValidationReason> invalidReason = CartItemValidationEvaluator.resolveInvalidReason(
                    currentStatus,
                    context,
                    item.quantity(),
                    shopOnVacation
            );

            if (currentStatus == CartItemStatus.ACTIVE) {
                activeItemCount++;
            }
            if (invalidReason.isPresent()) {
                invalidItemCount++;
                warningReasons.add(invalidReason.get());
            } else {
                checkoutEligibleCount++;
                subtotal = subtotal.add(resolveLineTotal(context, item.quantity()));
            }

            items.add(toViewItem(item, context, currentStatus, invalidReason.orElse(null)));
        }

        boolean canCheckout = checkoutEligibleCount > 0 && invalidItemCount == 0;
        ViewCartSummary summary = new ViewCartSummary(
                activeItemCount,
                invalidItemCount,
                subtotal,
                canCheckout,
                buildWarnings(warningReasons)
        );

        return new ViewCartResult(cart.id(), items, summary, cart.createdAt(), cart.updatedAt());
    }

    public String successMessage() {
        return "Lay gio hang thanh cong.";
    }

    private ViewCartItem toViewItem(
            CartItem item,
            ProductPurchaseContext context,
            CartItemStatus status,
            CartItemValidationReason invalidReason
    ) {
        if (context == null) {
            return new ViewCartItem(
                    item.id(),
                    item.productId(),
                    item.sellerId(),
                    null,
                    null,
                    null,
                    item.quantity(),
                    status,
                    null,
                    false,
                    0,
                    invalidReason != null ? invalidReason.code() : CartItemValidationReason.PRODUCT_NOT_FOUND.code()
            );
        }

        ActiveProductPrice price = context.activePrice();
        BigDecimal effectivePrice = price != null ? price.effectivePrice() : null;
        boolean inStock = status == CartItemStatus.ACTIVE
                && context.stockQuantity() > 0
                && item.quantity() <= context.stockQuantity()
                && invalidReason == null;

        return new ViewCartItem(
                item.id(),
                item.productId(),
                context.sellerId(),
                context.shopId(),
                context.productTitle(),
                context.primaryImageUrl(),
                item.quantity(),
                status,
                effectivePrice,
                inStock,
                context.stockQuantity(),
                invalidReason != null ? invalidReason.code() : null
        );
    }

    private BigDecimal resolveLineTotal(ProductPurchaseContext context, int quantity) {
        if (context == null || context.activePrice() == null) {
            return BigDecimal.ZERO;
        }
        return context.activePrice().effectivePrice().multiply(BigDecimal.valueOf(quantity));
    }

    private List<String> buildWarnings(Set<CartItemValidationReason> reasons) {
        if (reasons.isEmpty()) {
            return List.of();
        }
        List<String> warnings = new ArrayList<>();
        if (reasons.contains(CartItemValidationReason.OUT_OF_STOCK)) {
            warnings.add("Co san pham het hang trong gio hang.");
        }
        if (reasons.contains(CartItemValidationReason.SHOP_ON_VACATION)) {
            warnings.add("Co san pham thuoc shop dang tam nghi.");
        }
        if (reasons.stream().anyMatch(INVALID_PRODUCT_REASONS::contains)) {
            warnings.add("Co san pham khong con kha dung trong gio hang.");
        }
        if (reasons.contains(CartItemValidationReason.ACTIVE_PRICE_MISSING)) {
            warnings.add("Co san pham chua co gia ban.");
        }
        return List.copyOf(warnings);
    }

    private static final EnumSet<CartItemValidationReason> INVALID_PRODUCT_REASONS = EnumSet.of(
            CartItemValidationReason.INVALID_PRODUCT,
            CartItemValidationReason.PRODUCT_NOT_ACTIVE,
            CartItemValidationReason.SHOP_NOT_ACTIVE,
            CartItemValidationReason.CATEGORY_INACTIVE,
            CartItemValidationReason.PRODUCT_NOT_FOUND
    );

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
