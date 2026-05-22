package com.twohands.commerce_service.application.cart.updatecartitemquantity;

import com.twohands.commerce_service.application.cart.addproducttocart.ProductSummaryResult;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class UpdateCartItemQuantityUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductPurchaseReadRepository productPurchaseReadRepository;
    private final Clock clock;

    public UpdateCartItemQuantityUseCase(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductPurchaseReadRepository productPurchaseReadRepository,
            Clock clock
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productPurchaseReadRepository = productPurchaseReadRepository;
        this.clock = clock;
    }

    @Transactional
    public UpdateCartItemQuantityResult execute(UpdateCartItemQuantityCommand command) {
        validateQuantity(command.quantity());

        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findByIdAndCartId(command.cartItemId(), cart.id())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.isRemoved()) {
            throw new AppException(
                    ErrorCode.INVALID_CART_ITEM,
                    "Removed cart item cannot be updated"
            );
        }

        ProductPurchaseContext context = productPurchaseReadRepository.findByProductId(cartItem.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        validatePurchasable(context);
        rejectWhenStockZero(context);
        rejectWhenQuantityExceedsStock(command.quantity(), context.stockQuantity());

        CartItemStatus itemStatus = CartItemStatus.ACTIVE;
        Instant now = clock.instant();

        CartItem updated = cartItemRepository.save(
                cartItem.withQuantityStatusAndSeller(command.quantity(), itemStatus, context.sellerId(), now)
        );
        cartRepository.updateTimestamp(cart.id(), now);

        int activeItemCount = cartItemRepository.findByCartIdExcludingRemoved(cart.id()).size();
        return toResult(cart.id(), updated, context, itemStatus, activeItemCount);
    }

    public String successMessage() {
        return "Cap nhat so luong san pham trong gio hang thanh cong.";
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "Quantity must be greater than 0",
                    "quantity",
                    "must be greater than 0"
            );
        }
    }

    private void validatePurchasable(ProductPurchaseContext context) {
        if (context.productStatus() != ProductStatus.ACTIVE) {
            throw new AppException(ErrorCode.NOT_PURCHASABLE, "Product is not available for purchase");
        }
        if (context.shopStatus() != ShopStatus.ACTIVE) {
            throw new AppException(ErrorCode.NOT_PURCHASABLE, "Shop is not available for purchase");
        }
        if (!context.categoryActive()) {
            throw new AppException(ErrorCode.NOT_PURCHASABLE, "Product category is not active");
        }
        if (context.activePrice() == null) {
            throw new AppException(ErrorCode.ACTIVE_PRICE_MISSING);
        }
    }

    private void rejectWhenStockZero(ProductPurchaseContext context) {
        if (context.stockQuantity() <= 0) {
            throw new AppException(ErrorCode.OUT_OF_STOCK, "Product is out of stock");
        }
    }

    private void rejectWhenQuantityExceedsStock(int quantity, int stockQuantity) {
        if (quantity > stockQuantity) {
            throw new AppException(
                    ErrorCode.OUT_OF_STOCK,
                    "Requested quantity exceeds available stock"
            );
        }
    }

    private UpdateCartItemQuantityResult toResult(
            UUID cartId,
            CartItem item,
            ProductPurchaseContext context,
            CartItemStatus status,
            int activeItemCount
    ) {
        var price = context.activePrice();
        boolean inStock = context.stockQuantity() > 0 && item.quantity() <= context.stockQuantity();
        ProductSummaryResult productSummary = new ProductSummaryResult(
                context.productId(),
                context.sellerId(),
                context.shopId(),
                context.productTitle(),
                context.primaryImageUrl(),
                price.price(),
                price.salePrice(),
                price.effectivePrice(),
                inStock,
                context.stockQuantity()
        );

        return new UpdateCartItemQuantityResult(
                cartId,
                item.id(),
                item.productId(),
                item.quantity(),
                status,
                productSummary,
                activeItemCount
        );
    }
}
