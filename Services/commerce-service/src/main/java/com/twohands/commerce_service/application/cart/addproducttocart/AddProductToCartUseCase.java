package com.twohands.commerce_service.application.cart.addproducttocart;

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

import java.time.Instant;
import java.util.UUID;

@Service
public class AddProductToCartUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductPurchaseReadRepository productPurchaseReadRepository;

    public AddProductToCartUseCase(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductPurchaseReadRepository productPurchaseReadRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productPurchaseReadRepository = productPurchaseReadRepository;
    }

    @Transactional
    public AddProductToCartResult execute(AddProductToCartCommand command) {
        validateQuantity(command.quantity());

        Cart cart = cartRepository.getOrCreateByUserId(command.userId());
        ProductPurchaseContext context = productPurchaseReadRepository.findByProductId(command.productId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        validatePurchasable(context);
        rejectWhenStockZero(context);

        CartItemStatus itemStatus = resolveItemStatus(command.quantity(), context.stockQuantity());
        Instant now = Instant.now();

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.id(), command.productId())
                .map(existing -> updateExistingItem(existing, command.quantity(), context.sellerId(), itemStatus, now))
                .orElseGet(() -> new CartItem(
                        null,
                        cart.id(),
                        command.productId(),
                        context.sellerId(),
                        command.quantity(),
                        itemStatus,
                        now,
                        now
                ));

        CartItem savedItem = cartItemRepository.save(cartItem);
        cartRepository.updateTimestamp(cart.id(), now);

        return toResult(cart.id(), savedItem, context, itemStatus);
    }

    public String successMessage() {
        return "Them san pham vao gio hang thanh cong.";
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

    private CartItemStatus resolveItemStatus(int quantity, int stockQuantity) {
        return quantity <= stockQuantity ? CartItemStatus.ACTIVE : CartItemStatus.OUT_OF_STOCK;
    }

    private CartItem updateExistingItem(
            CartItem existing,
            int quantity,
            UUID sellerId,
            CartItemStatus itemStatus,
            Instant updatedAt
    ) {
        return existing.withQuantityStatusAndSeller(quantity, itemStatus, sellerId, updatedAt);
    }

    private AddProductToCartResult toResult(UUID cartId, CartItem item, ProductPurchaseContext context, CartItemStatus status) {
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

        return new AddProductToCartResult(
                cartId,
                item.id(),
                item.productId(),
                item.quantity(),
                status,
                productSummary
        );
    }
}
