package com.twohands.commerce_service.unit.application.shipment;

import com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus.AdminOverrideShipmentStatusCommand;
import com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus.AdminOverrideShipmentStatusResult;
import com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus.AdminOverrideShipmentStatusUseCase;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusTransitionResult;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusTransitionService;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOverrideShipmentStatusUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-06-10T10:00:00Z");

    @Mock
    private ProcessGhnWebhookRepository processGhnWebhookRepository;

    @Mock
    private ShipmentStatusTransitionService shipmentStatusTransitionService;

    private AdminOverrideShipmentStatusUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AdminOverrideShipmentStatusUseCase(
                processGhnWebhookRepository,
                shipmentStatusTransitionService,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void execute_overridesShipmentStatus() {
        UUID shipmentId = UUID.randomUUID();
        SellerShipmentRecord shipment = sampleShipment(shipmentId, ShipmentStatus.SHIPPED);
        when(processGhnWebhookRepository.findByShipmentIdForUpdate(shipmentId)).thenReturn(Optional.of(shipment));
        when(shipmentStatusTransitionService.apply(
                eq(shipment),
                eq(ShipmentStatus.DELIVERED),
                eq("admin_override"),
                eq("TRACK-1")
        )).thenReturn(new ShipmentStatusTransitionResult(
                true,
                shipmentId,
                ShipmentStatus.SHIPPED,
                ShipmentStatus.DELIVERED,
                1
        ));

        AdminOverrideShipmentStatusResult result = useCase.execute(new AdminOverrideShipmentStatusCommand(
                shipmentId,
                "DELIVERED",
                "GHN webhook khong ve sau 48h, xac nhan giao hang",
                false
        ));

        assertThat(result.applied()).isTrue();
        assertEquals(ShipmentStatus.SHIPPED, result.previousStatus());
        assertEquals(ShipmentStatus.DELIVERED, result.currentStatus());
        assertEquals(1, result.orderItemsUpdated());
        assertEquals(NOW, result.occurredAt());
    }

    @Test
    void execute_returnsNoOpWhenStatusUnchanged() {
        UUID shipmentId = UUID.randomUUID();
        SellerShipmentRecord shipment = sampleShipment(shipmentId, ShipmentStatus.SHIPPED);
        when(processGhnWebhookRepository.findByShipmentIdForUpdate(shipmentId)).thenReturn(Optional.of(shipment));

        AdminOverrideShipmentStatusResult result = useCase.execute(new AdminOverrideShipmentStatusCommand(
                shipmentId,
                "SHIPPED",
                "No change needed for audit trail",
                false
        ));

        assertThat(result.applied()).isFalse();
        verify(shipmentStatusTransitionService, never()).apply(any(), any(), any(), any());
    }

    @Test
    void execute_rejectsShortReason() {
        UUID shipmentId = UUID.randomUUID();

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(new AdminOverrideShipmentStatusCommand(
                shipmentId,
                "DELIVERED",
                "short",
                false
        )));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
        verify(processGhnWebhookRepository, never()).findByShipmentIdForUpdate(shipmentId);
    }

    @Test
    void execute_rejectsInvalidTransition() {
        UUID shipmentId = UUID.randomUUID();
        SellerShipmentRecord shipment = sampleShipment(shipmentId, ShipmentStatus.PENDING);
        when(processGhnWebhookRepository.findByShipmentIdForUpdate(shipmentId)).thenReturn(Optional.of(shipment));

        AppException ex = assertThrows(AppException.class, () -> useCase.execute(new AdminOverrideShipmentStatusCommand(
                shipmentId,
                "DELIVERED",
                "GHN webhook khong ve sau 48h, xac nhan giao hang",
                false
        )));

        assertEquals(ErrorCode.INVALID_SHIPMENT_STATUS, ex.getErrorCode());
        verify(shipmentStatusTransitionService, never()).apply(any(), any(), any(), any());
    }

    private static SellerShipmentRecord sampleShipment(UUID shipmentId, ShipmentStatus status) {
        return new SellerShipmentRecord(
                shipmentId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ShipmentCarrier.GHN,
                ShipmentType.STANDARD,
                status,
                "GHN-001",
                "TRACK-1",
                BigDecimal.TEN,
                BigDecimal.ZERO,
                500,
                null,
                Instant.parse("2026-05-20T08:00:00Z"),
                null,
                Instant.parse("2026-05-19T00:00:00Z"),
                Instant.parse("2026-05-20T00:00:00Z")
        );
    }
}
