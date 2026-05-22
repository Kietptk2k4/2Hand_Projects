package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.updatesellershipment.UpdateSellerShipmentCommand;
import com.twohands.commerce_service.application.shipment.updatesellershipment.UpdateSellerShipmentTransactionService;
import com.twohands.commerce_service.application.shipment.updatesellershipment.UpdateSellerShipmentUseCase;
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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSellerShipmentUseCaseTest {

    @Mock
    private ManageSellerShipmentRepository manageSellerShipmentRepository;

    @Mock
    private UpdateSellerShipmentTransactionService updateSellerShipmentTransactionService;

    private UpdateSellerShipmentUseCase useCase;

    private final UUID sellerId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateSellerShipmentUseCase(
                manageSellerShipmentRepository,
                updateSellerShipmentTransactionService,
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void updatesManualShipmentFromPendingToReadyToShip() {
        SellerShipmentRecord pending = manualRecord(ShipmentStatus.PENDING);
        SellerShipmentRecord ready = manualRecord(ShipmentStatus.READY_TO_SHIP);
        SellerShipmentDetail detail = detail(ready);

        when(manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(pending));
        when(updateSellerShipmentTransactionService.applyStatusChange(
                pending, ShipmentStatus.READY_TO_SHIP, null, now))
                .thenReturn(ready);
        when(manageSellerShipmentRepository.findDetailForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(detail));

        SellerShipmentDetail result = useCase.execute(new UpdateSellerShipmentCommand(
                sellerId, shipmentId, "READY_TO_SHIP", null
        ));

        assertThat(result.shipment().status()).isEqualTo(ShipmentStatus.READY_TO_SHIP);
        verify(updateSellerShipmentTransactionService).applyStatusChange(
                pending, ShipmentStatus.READY_TO_SHIP, null, now
        );
    }

    @Test
    void rejectsGhnShipmentUpdate() {
        SellerShipmentRecord ghn = new SellerShipmentRecord(
                shipmentId, orderId, sellerId,
                ShipmentCarrier.GHN, ShipmentType.STANDARD, ShipmentStatus.PENDING,
                "GHN-1", "TRK-1", BigDecimal.TEN, BigDecimal.ZERO, 500,
                null, null, null, now, now
        );
        when(manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(ghn));

        assertThatThrownBy(() -> useCase.execute(new UpdateSellerShipmentCommand(
                sellerId, shipmentId, "SHIPPED", null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHIPMENT_CARRIER_NOT_EDITABLE);

        verify(updateSellerShipmentTransactionService, never()).applyStatusChange(any(), any(), any(), any());
    }

    @Test
    void rejectsInvalidTransition() {
        SellerShipmentRecord shipped = manualRecord(ShipmentStatus.SHIPPED);
        when(manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(shipped));

        assertThatThrownBy(() -> useCase.execute(new UpdateSellerShipmentCommand(
                sellerId, shipmentId, "PENDING", null
        )))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_SHIPMENT_STATUS);
    }

    @Test
    void updatesTrackingOnly() {
        SellerShipmentRecord pending = manualRecord(ShipmentStatus.PENDING);
        SellerShipmentRecord updated = new SellerShipmentRecord(
                shipmentId, orderId, sellerId,
                ShipmentCarrier.MANUAL, ShipmentType.STANDARD, ShipmentStatus.PENDING,
                null, "TRK-NEW", BigDecimal.TEN, BigDecimal.ZERO, 500,
                null, null, null, now, now
        );

        when(manageSellerShipmentRepository.findShipmentForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(pending));
        when(updateSellerShipmentTransactionService.applyTrackingOnly(pending, "TRK-NEW", now))
                .thenReturn(updated);
        when(manageSellerShipmentRepository.findDetailForSeller(shipmentId, sellerId))
                .thenReturn(Optional.of(detail(updated)));

        SellerShipmentDetail result = useCase.execute(new UpdateSellerShipmentCommand(
                sellerId, shipmentId, null, "TRK-NEW"
        ));

        assertThat(result.shipment().trackingNumber()).isEqualTo("TRK-NEW");
        verify(updateSellerShipmentTransactionService).applyTrackingOnly(pending, "TRK-NEW", now);
        verify(updateSellerShipmentTransactionService, never()).applyStatusChange(any(), any(), any(), any());
    }

    private SellerShipmentRecord manualRecord(ShipmentStatus status) {
        return new SellerShipmentRecord(
                shipmentId, orderId, sellerId,
                ShipmentCarrier.MANUAL, ShipmentType.STANDARD, status,
                null, null, BigDecimal.TEN, BigDecimal.ZERO, 500,
                null, null, null, now, now
        );
    }

    private SellerShipmentDetail detail(SellerShipmentRecord shipment) {
        return new SellerShipmentDetail(
                shipment,
                new ShipmentAddressSnapshot(
                        "Buyer", "0900000000", "79", "760", "26734", "123 Street", "Full"
                ),
                List.of(new ShipmentOrderItemSummary(
                        UUID.randomUUID(), "Product", 1, "PROCESSING"
                ))
        );
    }
}
