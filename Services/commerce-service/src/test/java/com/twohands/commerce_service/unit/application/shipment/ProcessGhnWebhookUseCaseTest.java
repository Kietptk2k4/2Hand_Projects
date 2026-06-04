package com.twohands.commerce_service.unit.application.shipment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.commerce_service.application.shipment.common.ShipmentLifecycleOutboxEmitter;
import com.twohands.commerce_service.application.shipment.common.ShipmentStatusChangedOutboxService;
import com.twohands.commerce_service.application.shipment.processghnwebhook.ProcessGhnWebhookResult;
import com.twohands.commerce_service.application.shipment.processghnwebhook.ProcessGhnWebhookUseCase;
import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.shipment.GhnWebhookLogRepository;
import com.twohands.commerce_service.domain.shipment.ProcessGhnWebhookRepository;
import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import com.twohands.commerce_service.infrastructure.ghn.GhnWebhookSignatureVerifier;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessGhnWebhookUseCaseTest {

    @Mock
    private GhnWebhookSignatureVerifier signatureVerifier;

    @Mock
    private GhnWebhookLogRepository ghnWebhookLogRepository;

    @Mock
    private ProcessGhnWebhookRepository processGhnWebhookRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ShipmentStatusChangedOutboxService shipmentStatusChangedOutboxService;

    @Mock
    private ShipmentLifecycleOutboxEmitter shipmentLifecycleOutboxEmitter;

    private ProcessGhnWebhookUseCase useCase;

    private final UUID logId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-05-21T10:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ProcessGhnWebhookUseCase(
                signatureVerifier,
                ghnWebhookLogRepository,
                processGhnWebhookRepository,
                outboxEventRepository,
                shipmentStatusChangedOutboxService,
                shipmentLifecycleOutboxEmitter,
                new ObjectMapper(),
                Clock.fixed(now, ZoneOffset.UTC)
        );
    }

    @Test
    void updatesShipmentToDeliveredAndOrderItems() {
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("OrderCode", "GHN-123");
        body.put("Status", "delivered");

        SellerShipmentRecord shipment = shipment(ShipmentStatus.SHIPPED);
        when(signatureVerifier.verify(null, null)).thenReturn(true);
        when(ghnWebhookLogRepository.insertLog(eq("GHN-123"), eq("delivered"), any())).thenReturn(logId);
        when(processGhnWebhookRepository.findByGhnOrderCodeForUpdate("GHN-123"))
                .thenReturn(Optional.of(shipment));
        when(processGhnWebhookRepository.updateStatus(
                shipmentId, ShipmentStatus.SHIPPED, ShipmentStatus.DELIVERED, now))
                .thenReturn(true);
        when(shipmentStatusChangedOutboxService.build(any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        ProcessGhnWebhookResult result = useCase.execute(body, null, null);

        assertThat(result.statusChanged()).isTrue();
        assertThat(result.newStatus()).isEqualTo(ShipmentStatus.DELIVERED);
        verify(processGhnWebhookRepository).updateOrderItemsForShipment(shipmentId, "DELIVERED", now);
        verify(ghnWebhookLogRepository).markProcessed(logId);
        verify(outboxEventRepository).save(any(OutboxEvent.class));
        verify(shipmentLifecycleOutboxEmitter).emitDedicatedNotificationEvents(
                shipment,
                ShipmentStatus.DELIVERED,
                now,
                null
        );
    }

    @Test
    void emitsDedicatedShippedOutboxWhenTransitioningToShipped() {
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("OrderCode", "GHN-123");
        body.put("Status", "picked");

        SellerShipmentRecord shipment = shipment(ShipmentStatus.READY_TO_SHIP);
        when(signatureVerifier.verify(null, null)).thenReturn(true);
        when(ghnWebhookLogRepository.insertLog(eq("GHN-123"), eq("picked"), any())).thenReturn(logId);
        when(processGhnWebhookRepository.findByGhnOrderCodeForUpdate("GHN-123"))
                .thenReturn(Optional.of(shipment));
        when(processGhnWebhookRepository.updateStatus(
                shipmentId, ShipmentStatus.READY_TO_SHIP, ShipmentStatus.SHIPPED, now))
                .thenReturn(true);
        when(shipmentStatusChangedOutboxService.build(any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleOutbox());

        ProcessGhnWebhookResult result = useCase.execute(body, null, null);

        assertThat(result.newStatus()).isEqualTo(ShipmentStatus.SHIPPED);
        verify(shipmentLifecycleOutboxEmitter).emitDedicatedNotificationEvents(
                shipment,
                ShipmentStatus.SHIPPED,
                now,
                null
        );
    }

    @Test
    void isIdempotentWhenStatusUnchanged() {
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("OrderCode", "GHN-123");
        body.put("Status", "delivered");

        SellerShipmentRecord shipment = shipment(ShipmentStatus.DELIVERED);
        when(signatureVerifier.verify(null, null)).thenReturn(true);
        when(ghnWebhookLogRepository.insertLog(any(), any(), any())).thenReturn(logId);
        when(processGhnWebhookRepository.findByGhnOrderCodeForUpdate("GHN-123"))
                .thenReturn(Optional.of(shipment));

        ProcessGhnWebhookResult result = useCase.execute(body, null, null);

        assertThat(result.processed()).isTrue();
        assertThat(result.statusChanged()).isFalse();
        verify(processGhnWebhookRepository, never()).updateStatus(any(), any(), any(), any());
        verify(outboxEventRepository, never()).save(any());
        verify(shipmentLifecycleOutboxEmitter, never()).emitDedicatedNotificationEvents(any(), any(), any(), any());
    }

    @Test
    void leavesLogUnprocessedWhenShipmentNotFound() {
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("OrderCode", "GHN-MISSING");
        body.put("Status", "delivered");

        when(signatureVerifier.verify(null, null)).thenReturn(true);
        when(ghnWebhookLogRepository.insertLog(any(), any(), any())).thenReturn(logId);
        when(processGhnWebhookRepository.findByGhnOrderCodeForUpdate("GHN-MISSING"))
                .thenReturn(Optional.empty());

        ProcessGhnWebhookResult result = useCase.execute(body, null, null);

        assertThat(result.shipmentFound()).isFalse();
        assertThat(result.processed()).isFalse();
        verify(ghnWebhookLogRepository, never()).markProcessed(any());
    }

    @Test
    void rejectsInvalidSignature() {
        ObjectNode body = new ObjectMapper().createObjectNode();
        body.put("OrderCode", "GHN-123");
        body.put("Status", "delivered");

        when(signatureVerifier.verify("bad", null)).thenReturn(false);
        when(ghnWebhookLogRepository.insertLog(any(), any(), any())).thenReturn(logId);

        ProcessGhnWebhookResult result = useCase.execute(body, "bad", null);

        assertThat(result.signatureValid()).isFalse();
        assertThat(result.processed()).isFalse();
        verify(processGhnWebhookRepository, never()).findByGhnOrderCodeForUpdate(any());
    }

    private SellerShipmentRecord shipment(ShipmentStatus status) {
        return new SellerShipmentRecord(
                shipmentId, orderId, sellerId,
                ShipmentCarrier.GHN, ShipmentType.STANDARD, status,
                "GHN-123", "TRK-1", BigDecimal.TEN, BigDecimal.ZERO, 500,
                null, now, null, now, now
        );
    }

    private OutboxEvent sampleOutbox() {
        return new OutboxEvent(
                UUID.randomUUID(),
                ShipmentStatusChangedOutboxService.EVENT_TYPE,
                "shipment:test",
                shipmentId,
                "commerce",
                "{}",
                com.twohands.commerce_service.domain.outbox.OutboxStatus.PENDING,
                0,
                now,
                null,
                null
        );
    }
}
