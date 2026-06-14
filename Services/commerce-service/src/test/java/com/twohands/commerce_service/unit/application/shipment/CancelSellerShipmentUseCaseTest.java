package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.cancelsellershipment.CancelSellerShipmentUseCase;
import com.twohands.commerce_service.application.shipment.common.GhnShipmentStatusUpdateService;
import com.twohands.commerce_service.application.shipment.common.SellerShipmentBuyerEnrichmentService;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusTransitionService;
import com.twohands.commerce_service.domain.order.CommerceBuyerSummary;
import com.twohands.commerce_service.domain.shipment.GhnCancelOrderGateway;
import com.twohands.commerce_service.domain.shipment.ManageSellerShipmentRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentDetail;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemSummary;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelSellerShipmentUseCaseTest {

    @Mock
    private ManageSellerShipmentRepository manageSellerShipmentRepository;
    @Mock
    private GhnCancelOrderGateway ghnCancelOrderGateway;
    @Mock
    private GhnShipmentStatusUpdateService ghnShipmentStatusUpdateService;
    @Mock
    private ShipmentStatusTransitionService shipmentStatusTransitionService;
    @Mock
    private SellerShipmentBuyerEnrichmentService sellerShipmentBuyerEnrichmentService;

    private CancelSellerShipmentUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-06-04T18:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new CancelSellerShipmentUseCase(
                manageSellerShipmentRepository,
                ghnCancelOrderGateway,
                ghnShipmentStatusUpdateService,
                shipmentStatusTransitionService,
                sellerShipmentBuyerEnrichmentService
        );
    }

    @Test
    void cancelsManualShipment() {
        SellerShipmentRecord pending = manualRecord(ShipmentStatus.PENDING);
        SellerShipmentDetail detail = detail(pending);

        when(manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(pending));
        when(manageSellerShipmentRepository.findDetailForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(detail));
        when(sellerShipmentBuyerEnrichmentService.enrich(detail)).thenReturn(detail);

        SellerShipmentDetail result = useCase.execute(
                new CancelSellerShipmentUseCase.CancelSellerShipmentCommand(sellerId, shipmentId)
        );

        assertThat(result.shipment().status()).isEqualTo(ShipmentStatus.PENDING);
        verify(shipmentStatusTransitionService).apply(
                eq(pending),
                eq(ShipmentStatus.CANCELLED),
                eq("seller_cancel"),
                eq((String) null)
        );
    }

    @Test
    void cancelsGhnShipment() {
        SellerShipmentRecord ghn = ghnRecord(ShipmentStatus.READY_TO_SHIP);
        SellerShipmentDetail detail = detail(ghn);

        when(manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(ghn));
        when(manageSellerShipmentRepository.findDetailForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(detail));
        when(sellerShipmentBuyerEnrichmentService.enrich(detail)).thenReturn(detail);

        useCase.execute(new CancelSellerShipmentUseCase.CancelSellerShipmentCommand(sellerId, shipmentId));

        verify(ghnCancelOrderGateway).cancelOrder("GHN-123");
        verify(ghnShipmentStatusUpdateService).apply(ghn, "cancel", "GHN-123");
    }

    @Test
    void rejectsShippedShipment() {
        SellerShipmentRecord shipped = manualRecord(ShipmentStatus.SHIPPED);
        when(manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(shipped));

        assertThatThrownBy(() -> useCase.execute(
                new CancelSellerShipmentUseCase.CancelSellerShipmentCommand(sellerId, shipmentId)
        ))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SHIPMENT_STATUS);
    }

    private SellerShipmentRecord manualRecord(ShipmentStatus status) {
        return new SellerShipmentRecord(
                shipmentId, orderId, sellerId,
                ShipmentCarrier.MANUAL, ShipmentType.STANDARD, status,
                null, null, BigDecimal.TEN, BigDecimal.ZERO, 500,
                null, null, null, now, now
        );
    }

    private SellerShipmentRecord ghnRecord(ShipmentStatus status) {
        return new SellerShipmentRecord(
                shipmentId, orderId, sellerId,
                ShipmentCarrier.GHN, ShipmentType.STANDARD, status,
                "GHN-123", "TRK-1", BigDecimal.TEN, BigDecimal.ZERO, 500,
                null, null, null, now, now
        );
    }

    private SellerShipmentDetail detail(SellerShipmentRecord shipment) {
        return new SellerShipmentDetail(
                shipment,
                new ShipmentAddressSnapshot("Buyer", "0900000000", "79", "760", "26734", "123", "Full"),
                List.of(new ShipmentOrderItemSummary(UUID.randomUUID(), "Product", 1, "PROCESSING")),
                CommerceBuyerSummary.empty()
        );
    }
}
