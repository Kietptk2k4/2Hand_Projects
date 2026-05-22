package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.viewshipment.ViewShipmentCommand;
import com.twohands.commerce_service.application.shipment.viewshipment.ViewShipmentUseCase;
import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentOrderItemSummary;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ViewShipmentRepository;
import com.twohands.commerce_service.domain.shipment.ViewShipmentResult;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewShipmentUseCaseTest {

    @Mock
    private ViewShipmentRepository viewShipmentRepository;

    @InjectMocks
    private ViewShipmentUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();

    @Test
    void shouldReturnShipmentDetailForBuyer() {
        when(viewShipmentRepository.findByShipmentIdAndUserId(shipmentId, buyerId))
                .thenReturn(Optional.of(detail(ShipmentAccessRole.BUYER)));

        ViewShipmentResult result = useCase.execute(new ViewShipmentCommand(buyerId, shipmentId));

        assertThat(result.accessedAs()).isEqualTo(ShipmentAccessRole.BUYER);
        assertThat(result.shipment().trackingNumber()).isEqualTo("TRACK-001");
        assertThat(result.orderItems()).hasSize(1);
        assertThat(result.addressSnapshot().fullAddress()).contains("TP.HCM");
    }

    @Test
    void shouldReturnShipmentDetailForSeller() {
        when(viewShipmentRepository.findByShipmentIdAndUserId(shipmentId, sellerId))
                .thenReturn(Optional.of(detail(ShipmentAccessRole.SELLER)));

        ViewShipmentResult result = useCase.execute(new ViewShipmentCommand(sellerId, shipmentId));

        assertThat(result.accessedAs()).isEqualTo(ShipmentAccessRole.SELLER);
    }

    @Test
    void shouldThrowWhenShipmentNotFoundOrNotOwned() {
        when(viewShipmentRepository.findByShipmentIdAndUserId(shipmentId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewShipmentCommand(buyerId, shipmentId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHIPMENT_NOT_FOUND);
    }

    private ViewShipmentResult detail(ShipmentAccessRole role) {
        Instant now = Instant.parse("2026-05-21T10:00:00Z");
        return new ViewShipmentResult(
                new SellerShipmentRecord(
                        shipmentId,
                        orderId,
                        sellerId,
                        ShipmentCarrier.GHN,
                        ShipmentType.STANDARD,
                        ShipmentStatus.SHIPPED,
                        "GHN123",
                        "TRACK-001",
                        BigDecimal.valueOf(50_000),
                        BigDecimal.ZERO,
                        500,
                        null,
                        now,
                        null,
                        now,
                        now
                ),
                new ShipmentAddressSnapshot(
                        "Nguyen Van A",
                        "0901234567",
                        "79",
                        "760",
                        "26734",
                        "123 Nguyen Van Linh",
                        "123 Nguyen Van Linh, Q.7, TP.HCM"
                ),
                List.of(new ShipmentOrderItemSummary(
                        UUID.randomUUID(),
                        "iPhone 15",
                        1,
                        "SHIPPED"
                )),
                List.of(),
                role
        );
    }
}
