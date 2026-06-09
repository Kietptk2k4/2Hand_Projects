package com.twohands.commerce_service.unit.application.shipping;

import com.twohands.commerce_service.application.shipping.CartShippingQuoteContext;
import com.twohands.commerce_service.application.shipping.CartShippingQuoteLoader;
import com.twohands.commerce_service.application.shipping.SellerWeightGroup;
import com.twohands.commerce_service.application.shipping.ShippingFeeQuoteService;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeCommand;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeResult;
import com.twohands.commerce_service.application.shipping.calculateshippingfee.CalculateShippingFeeUseCase;
import com.twohands.commerce_service.domain.address.UserAddress;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfile;
import com.twohands.commerce_service.domain.shipping.SellerShippingProfileRepository;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shipping.ShippingGroupFeeQuote;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateShippingFeeUseCaseTest {

    @Mock
    private CartShippingQuoteLoader cartShippingQuoteLoader;
    @Mock
    private SellerShippingProfileRepository sellerShippingProfileRepository;
    @Mock
    private ShippingFeeQuoteService shippingFeeQuoteService;

    @InjectMocks
    private CalculateShippingFeeUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shopId = UUID.randomUUID();

    @Test
    void shouldReturnShippingFeePerSellerGroup() {
        UserAddress address = new UserAddress(addressId, userId, "79", "760", "26734");
        SellerWeightGroup weightGroup = new SellerWeightGroup(sellerId, shopId, 1500);
        when(cartShippingQuoteLoader.load(eq(userId), any(), eq(addressId), eq(ShipmentType.EXPRESS)))
                .thenReturn(new CartShippingQuoteContext(address, ShipmentType.EXPRESS, List.of(weightGroup)));
        when(sellerShippingProfileRepository.findByShopIds(any()))
                .thenReturn(Map.of(shopId, new SellerShippingProfile(shopId, sellerId, "79", "760", "26734")));
        when(shippingFeeQuoteService.quoteGroup(any(), any(), any(), any(), eq(1500), eq(ShipmentType.EXPRESS)))
                .thenReturn(new ShippingGroupFeeQuote(
                        BigDecimal.valueOf(52_500),
                        BigDecimal.valueOf(52_500),
                        LocalDate.of(2026, 5, 22)
                ));

        CalculateShippingFeeResult result = useCase.execute(new CalculateShippingFeeCommand(
                userId,
                List.of(cartItemId),
                addressId,
                ShipmentType.EXPRESS
        ));

        assertThat(result.totalShippingFee()).isEqualByComparingTo(BigDecimal.valueOf(52_500));
        assertThat(result.sellerGroups()).hasSize(1);
        assertThat(result.sellerGroups().getFirst().shippingFee()).isEqualByComparingTo(BigDecimal.valueOf(52_500));
        assertThat(result.sellerGroups().getFirst().shippingFeeOrigin()).isEqualByComparingTo(BigDecimal.valueOf(52_500));
        assertThat(result.sellerGroups().getFirst().estimatedDeliveryDate()).isEqualTo(LocalDate.of(2026, 5, 22));
        assertThat(result.sellerGroups().getFirst().shipmentType()).isEqualTo(ShipmentType.EXPRESS);
    }

    @Test
    void shouldRejectWhenShippingProfileMissing() {
        UserAddress address = new UserAddress(addressId, userId, "79", "760", "26734");
        when(cartShippingQuoteLoader.load(any(), any(), any(), any()))
                .thenReturn(new CartShippingQuoteContext(
                        address,
                        ShipmentType.STANDARD,
                        List.of(new SellerWeightGroup(sellerId, shopId, 500))
                ));
        when(sellerShippingProfileRepository.findByShopIds(any())).thenReturn(Map.of());

        assertThatThrownBy(() -> useCase.execute(new CalculateShippingFeeCommand(
                userId, List.of(cartItemId), addressId, null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHIPPING_PROFILE_MISSING);
    }
}
