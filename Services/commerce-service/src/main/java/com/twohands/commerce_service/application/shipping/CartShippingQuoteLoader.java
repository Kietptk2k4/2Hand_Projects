package com.twohands.commerce_service.application.shipping;

import com.twohands.commerce_service.domain.address.UserAddress;
import com.twohands.commerce_service.domain.address.UserAddressRepository;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CartShippingQuoteLoader {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProductPurchaseReadRepository productPurchaseReadRepository;

    public CartShippingQuoteLoader(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserAddressRepository userAddressRepository,
            ProductPurchaseReadRepository productPurchaseReadRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userAddressRepository = userAddressRepository;
        this.productPurchaseReadRepository = productPurchaseReadRepository;
    }

    public CartShippingQuoteContext load(
            UUID userId,
            List<UUID> cartItemIds,
            UUID addressId,
            ShipmentType shipmentType
    ) {
        validateCartItemIds(cartItemIds);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart not found for user"));

        List<CartItem> cartItems = loadOwnedCartItems(cart.id(), cartItemIds);
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        ShipmentType resolvedType = shipmentType == null ? ShipmentType.STANDARD : shipmentType;

        Map<UUID, ProductPurchaseContext> productsById = productPurchaseReadRepository.findByProductIds(
                cartItems.stream().map(CartItem::productId).distinct().toList()
        );

        List<SellerWeightGroup> sellerGroups = groupBySeller(cartItems, productsById);

        return new CartShippingQuoteContext(address, resolvedType, sellerGroups);
    }

    private void validateCartItemIds(List<UUID> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new AppException(
                    ErrorCode.VALIDATION_ERROR,
                    "At least one cart item is required",
                    "cart_item_ids",
                    "must not be empty"
            );
        }
    }

    private List<CartItem> loadOwnedCartItems(UUID cartId, List<UUID> cartItemIds) {
        List<UUID> distinctIds = cartItemIds.stream().distinct().toList();
        List<CartItem> loaded = cartItemRepository.findByCartIdAndIds(cartId, distinctIds);
        if (loaded.size() != distinctIds.size()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "One or more cart items were not found");
        }
        return loaded;
    }

    private List<SellerWeightGroup> groupBySeller(
            List<CartItem> cartItems,
            Map<UUID, ProductPurchaseContext> productsById
    ) {
        Map<UUID, SellerWeightAccumulator> accumulators = new LinkedHashMap<>();

        for (CartItem cartItem : cartItems) {
            ProductPurchaseContext product = productsById.get(cartItem.productId());
            if (product == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            int lineWeight = product.weightGram() * cartItem.quantity();
            accumulators.computeIfAbsent(
                    cartItem.sellerId(),
                    sellerId -> new SellerWeightAccumulator(sellerId, product.shopId())
            ).addWeight(lineWeight);
        }

        return accumulators.values().stream()
                .map(SellerWeightAccumulator::toGroup)
                .toList();
    }

    private static final class SellerWeightAccumulator {
        private final UUID sellerId;
        private final UUID shopId;
        private int totalWeightGram;

        private SellerWeightAccumulator(UUID sellerId, UUID shopId) {
            this.sellerId = sellerId;
            this.shopId = shopId;
        }

        private void addWeight(int weightGram) {
            this.totalWeightGram += weightGram;
        }

        private SellerWeightGroup toGroup() {
            return new SellerWeightGroup(sellerId, shopId, totalWeightGram);
        }
    }
}
