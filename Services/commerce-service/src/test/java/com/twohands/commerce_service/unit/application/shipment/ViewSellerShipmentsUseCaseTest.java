package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.viewsellershipments.ViewSellerShipmentsCommand;
import com.twohands.commerce_service.application.shipment.viewsellershipments.ViewSellerShipmentsUseCase;
import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.shipment.SellerShipmentListEntry;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.ViewSellerShipmentsRepository;
import com.twohands.commerce_service.domain.shipment.ViewSellerShipmentsResult;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.domain.shop.SellerShop;
import com.twohands.commerce_service.domain.shop.SellerShopRepository;
import com.twohands.commerce_service.domain.shop.ShopStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewSellerShipmentsUseCaseTest {

    @Mock
    private SellerShopRepository sellerShopRepository;

    @Mock
    private ViewSellerShipmentsRepository viewSellerShipmentsRepository;

    private ViewSellerShipmentsUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ViewSellerShipmentsUseCase(sellerShopRepository, viewSellerShipmentsRepository);
    }

    @Test
    void shouldReturnPaginatedShipmentsWhenSellerHasShop() {
        SellerShop shop = new SellerShop(UUID.randomUUID(), sellerId, ShopStatus.ACTIVE);
        SellerShipmentListEntry entry = new SellerShipmentListEntry(
                UUID.randomUUID(),
                UUID.randomUUID(),
                ShipmentCarrier.MANUAL,
                ShipmentType.STANDARD,
                ShipmentStatus.PENDING,
                "TRK-1",
                null,
                "123 Street",
                Instant.parse("2026-05-21T10:00:00Z"),
                Instant.parse("2026-05-21T12:00:00Z"),
                2
        );

        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(shop));
        when(viewSellerShipmentsRepository.countBySellerId(eq(sellerId), eq(Optional.empty()), eq(Optional.empty())))
                .thenReturn(1L);
        when(viewSellerShipmentsRepository.findBySellerId(eq(sellerId), eq(Optional.empty()), eq(Optional.empty()), any()))
                .thenReturn(List.of(entry));
        when(viewSellerShipmentsRepository.countByStatusForSeller(sellerId))
                .thenReturn(Map.of("PENDING", 1L));

        ViewSellerShipmentsResult result = useCase.execute(
                new ViewSellerShipmentsCommand(sellerId, 1, 20, null, null)
        );

        assertThat(result.items()).containsExactly(entry);
        assertThat(result.pagination()).isEqualTo(PageMeta.of(1, 20, 1));
        assertThat(result.statusCounts()).containsEntry("PENDING", 1L);
    }

    @Test
    void shouldRejectWhenSellerHasNoShop() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewSellerShipmentsCommand(sellerId, 1, 20, null, null)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.SELLER_SHOP_NOT_FOUND));

        verify(viewSellerShipmentsRepository, never()).countBySellerId(any(), any(), any());
    }

    @Test
    void shouldRejectInvalidStatusFilter() {
        when(sellerShopRepository.findBySellerId(sellerId)).thenReturn(Optional.of(
                new SellerShop(UUID.randomUUID(), sellerId, ShopStatus.ACTIVE)
        ));

        assertThatThrownBy(() -> useCase.execute(new ViewSellerShipmentsCommand(sellerId, 1, 20, "BAD", null)))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }
}
