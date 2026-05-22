package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.viewshippingaddresssnapshot.ViewShippingAddressSnapshotCommand;
import com.twohands.commerce_service.application.shipment.viewshippingaddresssnapshot.ViewShippingAddressSnapshotUseCase;
import com.twohands.commerce_service.domain.shipment.ShipmentAccessRole;
import com.twohands.commerce_service.domain.shipment.ShipmentAddressSnapshot;
import com.twohands.commerce_service.domain.shipment.ViewShippingAddressSnapshotRepository;
import com.twohands.commerce_service.domain.shipment.ViewShippingAddressSnapshotResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewShippingAddressSnapshotUseCaseTest {

    @Mock
    private ViewShippingAddressSnapshotRepository viewShippingAddressSnapshotRepository;

    @InjectMocks
    private ViewShippingAddressSnapshotUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final UUID snapshotId = UUID.randomUUID();

    @Test
    void shouldReturnAddressSnapshotForBuyer() {
        when(viewShippingAddressSnapshotRepository.findByShipmentIdAndUserId(shipmentId, buyerId))
                .thenReturn(Optional.of(sampleResult(ShipmentAccessRole.BUYER)));

        ViewShippingAddressSnapshotResult result = useCase.execute(
                new ViewShippingAddressSnapshotCommand(buyerId, shipmentId)
        );

        assertThat(result.accessedAs()).isEqualTo(ShipmentAccessRole.BUYER);
        assertThat(result.addressSnapshot().fullAddress()).contains("TP.HCM");
        assertThat(result.snapshotId()).isEqualTo(snapshotId);
    }

    @Test
    void shouldThrowWhenShipmentOrSnapshotNotAccessible() {
        when(viewShippingAddressSnapshotRepository.findByShipmentIdAndUserId(shipmentId, buyerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ViewShippingAddressSnapshotCommand(buyerId, shipmentId)))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.SHIPMENT_NOT_FOUND);
    }

    private ViewShippingAddressSnapshotResult sampleResult(ShipmentAccessRole role) {
        return new ViewShippingAddressSnapshotResult(
                shipmentId,
                snapshotId,
                new ShipmentAddressSnapshot(
                        "Nguyen Van A",
                        "0901234567",
                        "79",
                        "760",
                        "26734",
                        "123 Nguyen Van Linh",
                        "123 Nguyen Van Linh, Q.7, TP.HCM"
                ),
                role,
                Instant.parse("2026-05-21T10:00:00Z")
        );
    }
}
