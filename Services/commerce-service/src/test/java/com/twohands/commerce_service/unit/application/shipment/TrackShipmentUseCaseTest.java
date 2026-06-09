package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.syncghnshipment.SyncGhnShipmentStatusUseCase;
import com.twohands.commerce_service.application.shipment.trackshipment.TrackShipmentCommand;
import com.twohands.commerce_service.application.shipment.trackshipment.TrackShipmentUseCase;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipment.TrackShipmentRepository;
import com.twohands.commerce_service.domain.shipment.TrackShipmentResult;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackShipmentUseCaseTest {

    @Mock
    private TrackShipmentRepository trackShipmentRepository;

    @Mock
    private SyncGhnShipmentStatusUseCase syncGhnShipmentStatusUseCase;

    @InjectMocks
    private TrackShipmentUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T15:00:00Z");

    @Test
    void shouldReturnTrackingForBuyer() {
        when(trackShipmentRepository.findByShipmentIdAndUserId(shipmentId, userId))
                .thenReturn(Optional.of(sampleResult(ShipmentAccessRole.BUYER)));

        TrackShipmentResult result = useCase.execute(new TrackShipmentCommand(userId, shipmentId));

        assertThat(result.accessedAs()).isEqualTo(ShipmentAccessRole.BUYER);
        assertThat(result.shipmentDelivered()).isTrue();
        assertThat(result.orderCompleted()).isFalse();
        assertThat(result.trackingNumber()).isEqualTo("TRACK-001");
    }

    @Test
    void shouldReturnTrackingForSeller() {
        when(trackShipmentRepository.findByShipmentIdAndUserId(shipmentId, userId))
                .thenReturn(Optional.of(sampleResult(ShipmentAccessRole.SELLER)));

        TrackShipmentResult result = useCase.execute(new TrackShipmentCommand(userId, shipmentId));

        assertThat(result.accessedAs()).isEqualTo(ShipmentAccessRole.SELLER);
    }

    @Test
    void shouldRejectWhenShipmentNotFoundOrNotAccessible() {
        when(trackShipmentRepository.findByShipmentIdAndUserId(shipmentId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new TrackShipmentCommand(userId, shipmentId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHIPMENT_NOT_FOUND);
    }

    private TrackShipmentResult sampleResult(ShipmentAccessRole role) {
        return new TrackShipmentResult(
                shipmentId,
                orderId,
                UUID.randomUUID(),
                role,
                ShipmentStatus.DELIVERED,
                ShipmentCarrier.GHN,
                ShipmentType.STANDARD,
                "TRACK-001",
                "GHN-123",
                now.minusSeconds(3600),
                now,
                null,
                OrderStatus.PROCESSING,
                true,
                false,
                List.of()
        );
    }
}
