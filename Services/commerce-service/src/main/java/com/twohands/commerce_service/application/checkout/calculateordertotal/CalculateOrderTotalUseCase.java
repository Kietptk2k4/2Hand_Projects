package com.twohands.commerce_service.application.checkout.calculateordertotal;

import com.twohands.commerce_service.application.shipping.ShippingFeeQuoteService;
import com.twohands.commerce_service.domain.address.UserAddress;
import com.twohands.commerce_service.domain.address.UserAddressRepository;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.checkout.OrderLineTotalCalculator;
import com.twohands.commerce_service.domain.checkout.ShippingFeeAllocator;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CalculateOrderTotalUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final ProductPurchaseReadRepository productPurchaseReadRepository;
    private final SellerShippingProfileRepository sellerShippingProfileRepository;
    private final ShippingFeeQuoteService shippingFeeQuoteService;

    public CalculateOrderTotalUseCase(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserAddressRepository userAddressRepository,
            ProductPurchaseReadRepository productPurchaseReadRepository,
            SellerShippingProfileRepository sellerShippingProfileRepository,
            ShippingFeeQuoteService shippingFeeQuoteService
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userAddressRepository = userAddressRepository;
        this.productPurchaseReadRepository = productPurchaseReadRepository;
        this.sellerShippingProfileRepository = sellerShippingProfileRepository;
        this.shippingFeeQuoteService = shippingFeeQuoteService;
    }

    @Transactional(readOnly = true)
    public CalculateOrderTotalResult execute(CalculateOrderTotalCommand command) {
        validateCartItemIds(command.cartItemIds());

        Cart cart = cartRepository.findByUserId(command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND, "Cart not found for user"));

        List<CartItem> cartItems = loadOwnedCartItems(cart.id(), command.cartItemIds());
        UserAddress address = userAddressRepository.findByIdAndUserId(command.addressId(), command.userId())
                .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));

        ShipmentType shipmentType = command.shipmentType() == null ? ShipmentType.STANDARD : command.shipmentType();

        Map<UUID, ProductPurchaseContext> productsById = productPurchaseReadRepository.findByProductIds(
                cartItems.stream().map(CartItem::productId).distinct().toList()
        );

        List<LineDraft> lineDrafts = buildLineDrafts(cartItems, productsById);
        Map<UUID, SellerGroupDraft> sellerGroups = groupBySeller(lineDrafts);

        Map<UUID, SellerShippingProfile> profiles = sellerShippingProfileRepository.findByShopIds(
                sellerGroups.values().stream().map(SellerGroupDraft::shopId).distinct().toList()
        );

        List<SellerShippingGroupResult> sellerShippingGroupResults = new ArrayList<>();
        BigDecimal totalShippingFee = BigDecimal.ZERO;

        for (SellerGroupDraft group : sellerGroups.values()) {
            SellerShippingProfile profile = profiles.get(group.shopId());
            if (profile == null) {
                throw new AppException(
                        ErrorCode.SHIPPING_PROFILE_MISSING,
                        "Seller shipping profile is missing for shop " + group.shopId()
                );
            }

            BigDecimal groupFee = shippingFeeQuoteService.quoteGroupFee(
                    profile,
                    address.provinceCode(),
                    address.districtCode(),
                    group.totalWeightGram(),
                    shipmentType
            );
            totalShippingFee = totalShippingFee.add(groupFee);

            List<BigDecimal> itemTotals = group.lines().stream().map(LineDraft::itemTotal).toList();
            List<BigDecimal> allocations = ShippingFeeAllocator.allocateProportionally(groupFee, itemTotals);
            for (int i = 0; i < group.lines().size(); i++) {
                group.lines().get(i).setShippingFeeAllocated(allocations.get(i));
            }

            sellerShippingGroupResults.add(new SellerShippingGroupResult(
                    group.sellerId(),
                    group.shopId(),
                    groupFee,
                    shipmentType
            ));
        }

        BigDecimal totalAmount = lineDrafts.stream()
                .map(LineDraft::itemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<QuoteItemResult> items = lineDrafts.stream()
                .map(LineDraft::toQuoteItem)
                .toList();

        BigDecimal finalAmount = totalAmount.add(totalShippingFee);

        return new CalculateOrderTotalResult(
                items,
                totalAmount,
                totalShippingFee,
                finalAmount,
                sellerShippingGroupResults
        );
    }

    public String successMessage() {
        return "Tinh tong tien don hang thanh cong.";
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

    private List<LineDraft> buildLineDrafts(
            List<CartItem> cartItems,
            Map<UUID, ProductPurchaseContext> productsById
    ) {
        List<LineDraft> drafts = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            ProductPurchaseContext product = productsById.get(cartItem.productId());
            if (product == null) {
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
            ActiveProductPrice activePrice = product.activePrice();
            if (activePrice == null) {
                throw new AppException(ErrorCode.ACTIVE_PRICE_MISSING);
            }

            BigDecimal unitPrice = OrderLineTotalCalculator.unitPrice(activePrice);
            BigDecimal itemTotal = OrderLineTotalCalculator.itemTotal(activePrice, cartItem.quantity());

            drafts.add(new LineDraft(
                    cartItem.id(),
                    cartItem.productId(),
                    cartItem.sellerId(),
                    product.shopId(),
                    unitPrice,
                    cartItem.quantity(),
                    itemTotal,
                    product.weightGram() * cartItem.quantity(),
                    BigDecimal.ZERO
            ));
        }
        return drafts;
    }

    private Map<UUID, SellerGroupDraft> groupBySeller(List<LineDraft> lineDrafts) {
        Map<UUID, SellerGroupDraft> groups = new LinkedHashMap<>();
        for (LineDraft line : lineDrafts) {
            groups.computeIfAbsent(line.sellerId(), sellerId -> new SellerGroupDraft(sellerId, line.shopId()))
                    .lines()
                    .add(line);
        }
        return groups;
    }

    private static final class LineDraft {
        private final UUID cartItemId;
        private final UUID productId;
        private final UUID sellerId;
        private final UUID shopId;
        private final BigDecimal unitPrice;
        private final int quantity;
        private final BigDecimal itemTotal;
        private final int weightGram;
        private BigDecimal shippingFeeAllocated;

        private LineDraft(
                UUID cartItemId,
                UUID productId,
                UUID sellerId,
                UUID shopId,
                BigDecimal unitPrice,
                int quantity,
                BigDecimal itemTotal,
                int weightGram,
                BigDecimal shippingFeeAllocated
        ) {
            this.cartItemId = cartItemId;
            this.productId = productId;
            this.sellerId = sellerId;
            this.shopId = shopId;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.itemTotal = itemTotal;
            this.weightGram = weightGram;
            this.shippingFeeAllocated = shippingFeeAllocated;
        }

        private UUID sellerId() {
            return sellerId;
        }

        private UUID shopId() {
            return shopId;
        }

        private BigDecimal itemTotal() {
            return itemTotal;
        }

        private int weightGram() {
            return weightGram;
        }

        private void setShippingFeeAllocated(BigDecimal shippingFeeAllocated) {
            this.shippingFeeAllocated = shippingFeeAllocated;
        }

        private QuoteItemResult toQuoteItem() {
            return new QuoteItemResult(
                    cartItemId,
                    productId,
                    sellerId,
                    shopId,
                    unitPrice,
                    quantity,
                    itemTotal,
                    shippingFeeAllocated
            );
        }
    }

    private static final class SellerGroupDraft {
        private final UUID sellerId;
        private final UUID shopId;
        private final List<LineDraft> lines = new ArrayList<>();

        private SellerGroupDraft(UUID sellerId, UUID shopId) {
            this.sellerId = sellerId;
            this.shopId = shopId;
        }

        private UUID sellerId() {
            return sellerId;
        }

        private UUID shopId() {
            return shopId;
        }

        private List<LineDraft> lines() {
            return lines;
        }

        private int totalWeightGram() {
            return lines.stream().mapToInt(LineDraft::weightGram).sum();
        }
    }
}
