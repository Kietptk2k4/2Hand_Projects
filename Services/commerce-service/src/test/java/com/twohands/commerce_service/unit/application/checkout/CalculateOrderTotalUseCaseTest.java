package com.twohands.commerce_service.unit.application.checkout;

import com.twohands.commerce_service.application.checkout.calculateordertotal.CalculateOrderTotalCommand;
import com.twohands.commerce_service.application.checkout.calculateordertotal.CalculateOrderTotalResult;
import com.twohands.commerce_service.application.checkout.calculateordertotal.CalculateOrderTotalUseCase;
import com.twohands.commerce_service.application.shipping.ShippingFeeQuoteService;
import com.twohands.commerce_service.domain.address.UserAddress;
import com.twohands.commerce_service.domain.address.UserAddressRepository;
import com.twohands.commerce_service.domain.cart.Cart;
import com.twohands.commerce_service.domain.cart.CartItem;
import com.twohands.commerce_service.domain.cart.CartItemRepository;
import com.twohands.commerce_service.domain.cart.CartItemStatus;
import com.twohands.commerce_service.domain.cart.CartRepository;
import com.twohands.commerce_service.domain.catalog.ActiveProductPrice;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseContext;
import com.twohands.commerce_service.domain.catalog.ProductPurchaseReadRepository;
import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateOrderTotalUseCaseTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserAddressRepository userAddressRepository;
    @Mock
    private ProductPurchaseReadRepository productPurchaseReadRepository;
    @Mock
    private SellerShippingProfileRepository sellerShippingProfileRepository;
    @Mock
    private ShippingFeeQuoteService shippingFeeQuoteService;

    @InjectMocks
    private CalculateOrderTotalUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();

    @Test
    void shouldCalculateQuoteWithShippingAllocation() {
        Instant now = Instant.now();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
        when(cartItemRepository.findByCartIdAndIds(eq(cartId), any()))
                .thenReturn(List.of(new CartItem(
                        cartItemId, cartId, productId, sellerId, 2, CartItemStatus.ACTIVE, now, now
                )));
        when(userAddressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(new UserAddress(addressId, userId, "79", "760", "26734")));
        when(productPurchaseReadRepository.findByProductIds(any()))
                .thenReturn(Map.of(productId, productContext()));
        when(sellerShippingProfileRepository.findByShopIds(any()))
                .thenReturn(Map.of(shopId, new SellerShippingProfile(shopId, sellerId, "79", "760", "26734")));
        when(shippingFeeQuoteService.quoteGroupFee(any(), any(), any(), eq(1000), eq(ShipmentType.STANDARD)))
                .thenReturn(BigDecimal.valueOf(40_000));

        CalculateOrderTotalResult result = useCase.execute(new CalculateOrderTotalCommand(
                userId,
                List.of(cartItemId),
                addressId,
                ShipmentType.STANDARD
        ));

        assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1_800_000));
        assertThat(result.shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(40_000));
        assertThat(result.finalAmount()).isEqualByComparingTo(BigDecimal.valueOf(1_840_000));
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().unitPrice()).isEqualByComparingTo(BigDecimal.valueOf(900_000));
        assertThat(result.items().getFirst().shippingFeeAllocated()).isEqualByComparingTo(BigDecimal.valueOf(40_000));
        assertThat(result.sellerShippingGroups()).hasSize(1);
    }

    @Test
    void shouldRejectWhenCartItemNotFound() {
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, Instant.now(), Instant.now())));
        when(cartItemRepository.findByCartIdAndIds(eq(cartId), any())).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(new CalculateOrderTotalCommand(
                userId, List.of(cartItemId), addressId, null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CART_ITEM_NOT_FOUND);
    }

    @Test
    void shouldRejectWhenShippingProfileMissing() {
        Instant now = Instant.now();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(new Cart(cartId, userId, now, now)));
        when(cartItemRepository.findByCartIdAndIds(eq(cartId), any()))
                .thenReturn(List.of(new CartItem(
                        cartItemId, cartId, productId, sellerId, 1, CartItemStatus.ACTIVE, now, now
                )));
        when(userAddressRepository.findByIdAndUserId(addressId, userId))
                .thenReturn(Optional.of(new UserAddress(addressId, userId, "79", "760", "26734")));
        when(productPurchaseReadRepository.findByProductIds(any()))
                .thenReturn(Map.of(productId, productContext()));
        when(sellerShippingProfileRepository.findByShopIds(any())).thenReturn(Map.of());

        assertThatThrownBy(() -> useCase.execute(new CalculateOrderTotalCommand(
                userId, List.of(cartItemId), addressId, null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHIPPING_PROFILE_MISSING);
    }

    private ProductPurchaseContext productContext() {
        return new ProductPurchaseContext(
                productId,
                sellerId,
                shopId,
                "Phone",
                ProductStatus.ACTIVE,
                ShopStatus.ACTIVE,
                true,
                500,
                10,
                new ActiveProductPrice(
                        BigDecimal.valueOf(1_000_000),
                        BigDecimal.valueOf(900_000),
                        BigDecimal.valueOf(900_000)
                ),
                null
        );
    }
}
